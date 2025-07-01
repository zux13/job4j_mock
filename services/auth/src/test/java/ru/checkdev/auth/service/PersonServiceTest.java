package ru.checkdev.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.checkdev.auth.domain.Notify;
import ru.checkdev.auth.domain.Profile;
import ru.checkdev.auth.repository.PersonRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PersonServiceTest {

    @Mock
    private PersonRepository persons;
    @Mock
    private Messenger msg;

    private PersonService personService;
    private final PasswordEncoder encoding = new BCryptPasswordEncoder();

    @BeforeEach
    void setUp() {
        personService = new PersonService(persons, msg);
    }

    @Test
    void regSuccessfullyRegistersNewProfile() {
        Profile profile = new Profile("testuser", "test@example.com", "password");
        profile.setPrivacy(true);
        when(persons.save(any(Profile.class))).thenReturn(profile);

        Optional<Profile> result = personService.reg(profile);

        assertTrue(result.isPresent());
        assertEquals("test@example.com", result.get().getEmail());
        assertTrue(encoding.matches("password", result.get().getPassword()));
        assertNotNull(result.get().getKey());
        assertTrue(result.get().isActive());
        assertNull(result.get().getRoles());
        verify(persons, times(1)).save(any(Profile.class));
        verify(msg, times(1)).send(any(Notify.class));
    }

    @Test
    void regReturnsEmptyOptionalForDuplicateEmail() {
        Profile profile = new Profile("testuser", "test@example.com", "password");
        profile.setPrivacy(true);
        when(persons.save(any(Profile.class))).thenThrow(DataIntegrityViolationException.class);

        Optional<Profile> result = personService.reg(profile);

        assertTrue(result.isEmpty());
        verify(persons, times(1)).save(any(Profile.class));
        verify(msg, never()).send(any(Notify.class));
    }

    @Test
    void createSuccessfullyCreatesNewProfile() {
        Profile profile = new Profile("testuser", "test@example.com", "password");
        when(persons.save(any(Profile.class))).thenReturn(profile);

        Optional<Profile> result = personService.create(profile);

        assertTrue(result.isPresent());
        assertEquals("test@example.com", result.get().getEmail());
        assertTrue(encoding.matches("password", result.get().getPassword()));
        assertNotNull(result.get().getKey());
        assertTrue(result.get().isPrivacy());
        assertNull(result.get().getRoles());
        verify(persons, times(1)).save(any(Profile.class));
    }

    @Test
    void createReturnsEmptyOptionalForDuplicateEmail() {
        Profile profile = new Profile("testuser", "test@example.com", "password");
        when(persons.save(any(Profile.class))).thenThrow(DataIntegrityViolationException.class);

        Optional<Profile> result = personService.create(profile);

        assertTrue(result.isEmpty());
        verify(persons, times(1)).save(any(Profile.class));
    }

    @Test
    void findByEmailReturnsProfileWhenFound() {
        Profile profile = new Profile("testuser", "test@example.com", "password");
        when(persons.findByEmail("test@example.com")).thenReturn(profile);

        Optional<Profile> result = personService.findByEmail("test@example.com");

        assertTrue(result.isPresent());
        assertEquals("test@example.com", result.get().getEmail());
        verify(persons, times(1)).findByEmail("test@example.com");
    }

    @Test
    void findByEmailReturnsEmptyOptionalWhenNotFound() {
        when(persons.findByEmail("nonexistent@example.com")).thenReturn(null);

        Optional<Profile> result = personService.findByEmail("nonexistent@example.com");

        assertTrue(result.isEmpty());
        verify(persons, times(1)).findByEmail("nonexistent@example.com");
    }

    @Test
    void activatedSuccessfullyActivatesProfile() {
        Profile profile = new Profile("testuser", "test@example.com", "password");
        profile.setActive(false);
        when(persons.findByKey("testkey")).thenReturn(profile);
        when(persons.save(any(Profile.class))).thenReturn(profile);

        boolean result = personService.activated("testkey");

        assertTrue(result);
        assertTrue(profile.isActive());
        verify(persons, times(1)).findByKey("testkey");
        verify(persons, times(1)).save(profile);
    }

    @Test
    void activatedReturnsFalseForAlreadyActiveProfile() {
        Profile profile = new Profile("testuser", "test@example.com", "password");
        profile.setActive(true);
        when(persons.findByKey("testkey")).thenReturn(profile);

        boolean result = personService.activated("testkey");

        assertFalse(result);
        verify(persons, times(1)).findByKey("testkey");
        verify(persons, never()).save(any(Profile.class));
    }

    @Test
    void activatedReturnsFalseForNonExistentKey() {
        when(persons.findByKey("nonexistentkey")).thenReturn(null);

        boolean result = personService.activated("nonexistentkey");

        assertFalse(result);
        verify(persons, times(1)).findByKey("nonexistentkey");
        verify(persons, never()).save(any(Profile.class));
    }

    @Test
    void forgotSuccessfullyResetsPassword() {
        Profile profile = new Profile("testuser", "test@example.com", "oldpassword");
        profile.setPassword(encoding.encode("oldpassword"));
        when(persons.findByEmail("test@example.com")).thenReturn(profile);
        when(persons.save(any(Profile.class))).thenReturn(profile);

        Optional<Profile> result = personService.forgot(profile);

        assertTrue(result.isPresent());
        verify(persons, times(1)).findByEmail("test@example.com");
        verify(persons, times(1)).save(profile);
        verify(msg, times(1)).send(any(Notify.class));

        ArgumentCaptor<Profile> profileCaptor = ArgumentCaptor.forClass(Profile.class);
        verify(persons).save(profileCaptor.capture());
        Profile savedProfile = profileCaptor.getValue();
        assertFalse(encoding.matches("oldpassword", savedProfile.getPassword()));
    }

    @Test
    void forgotReturnsEmptyOptionalForNonExistentEmail() {
        Profile profile = new Profile("testuser", "nonexistent@example.com", "password");
        when(persons.findByEmail("nonexistent@example.com")).thenReturn(null);

        Optional<Profile> result = personService.forgot(profile);

        assertTrue(result.isEmpty());
        verify(persons, times(1)).findByEmail("nonexistent@example.com");
        verify(persons, never()).save(any(Profile.class));
        verify(msg, never()).send(any(Notify.class));
    }

    @Test
    void bindTelegramSuccessfullyBindsAccount() {
        Profile profile = new Profile("testuser", "test@example.com", "password");
        profile.setPassword(encoding.encode("password"));
        Long telegramId = 12345L;

        when(persons.findByEmail("test@example.com")).thenReturn(profile);
        when(persons.save(any(Profile.class))).thenReturn(profile);

        Optional<Profile> result = personService.bindTelegram("test@example.com", "password", telegramId);

        assertTrue(result.isPresent());
        assertEquals(telegramId, result.get().getTelegramId());
        verify(persons, times(1)).findByEmail("test@example.com");
        verify(persons, times(1)).save(profile);
    }

    @Test
    void bindTelegramReturnsEmptyOptionalForInvalidCredentials() {
        Profile profile = new Profile("testuser", "test@example.com", "wrongpassword");
        profile.setPassword(encoding.encode("correctpassword"));
        Long telegramId = 12345L;

        when(persons.findByEmail("test@example.com")).thenReturn(profile);

        Optional<Profile> result = personService.bindTelegram("test@example.com", "wrongpassword", telegramId);

        assertTrue(result.isEmpty());
        verify(persons, times(1)).findByEmail("test@example.com");
        verify(persons, never()).save(any(Profile.class));
    }

    @Test
    void bindTelegramReturnsEmptyOptionalForNonExistentEmail() {
        Long telegramId = 12345L;
        when(persons.findByEmail("nonexistent@example.com")).thenReturn(null);

        Optional<Profile> result = personService.bindTelegram("nonexistent@example.com", "password", telegramId);

        assertTrue(result.isEmpty());
        verify(persons, times(1)).findByEmail("nonexistent@example.com");
        verify(persons, never()).save(any(Profile.class));
    }

    @Test
    void unbindTelegramSuccessfullyUnbindsAccount() {
        Profile profile = new Profile("testuser", "test@example.com", "password");
        profile.setPassword(encoding.encode("password"));
        profile.setTelegramId(12345L);

        when(persons.findByEmail("test@example.com")).thenReturn(profile);
        when(persons.save(any(Profile.class))).thenReturn(profile);

        Optional<Profile> result = personService.unbindTelegram("test@example.com", "password");

        assertTrue(result.isPresent());
        assertNull(result.get().getTelegramId());
        verify(persons, times(1)).findByEmail("test@example.com");
        verify(persons, times(1)).save(profile);
    }

    @Test
    void unbindTelegramReturnsEmptyOptionalForInvalidCredentials() {
        Profile profile = new Profile("testuser", "test@example.com", "wrongpassword");
        profile.setPassword(encoding.encode("correctpassword"));
        profile.setTelegramId(12345L);

        when(persons.findByEmail("test@example.com")).thenReturn(profile);

        Optional<Profile> result = personService.unbindTelegram("test@example.com", "wrongpassword");

        assertTrue(result.isEmpty());
        verify(persons, times(1)).findByEmail("test@example.com");
        verify(persons, never()).save(any(Profile.class));
    }

    @Test
    void unbindTelegramReturnsEmptyOptionalForNonExistentEmail() {
        when(persons.findByEmail("nonexistent@example.com")).thenReturn(null);

        Optional<Profile> result = personService.unbindTelegram("nonexistent@example.com", "password");

        assertTrue(result.isEmpty());
        verify(persons, times(1)).findByEmail("nonexistent@example.com");
        verify(persons, never()).save(any(Profile.class));
    }

    @Test
    void findByTelegramIdReturnsListOfProfiles() {
        Profile profile1 = new Profile("user1", "user1@example.com", "pass1");
        profile1.setTelegramId(123L);
        Profile profile2 = new Profile("user2", "user2@example.com", "pass2");
        profile2.setTelegramId(123L);
        List<Profile> expectedProfiles = List.of(profile1, profile2);

        when(persons.findByTelegramId(123L)).thenReturn(expectedProfiles);

        List<Profile> actualProfiles = personService.findByTelegramId(123L);

        assertEquals(expectedProfiles.size(), actualProfiles.size());
        assertTrue(actualProfiles.containsAll(expectedProfiles));
        verify(persons, times(1)).findByTelegramId(123L);
    }

    @Test
    void findByTelegramIdReturnsEmptyListWhenNoProfilesFound() {
        when(persons.findByTelegramId(999L)).thenReturn(Collections.emptyList());

        List<Profile> actualProfiles = personService.findByTelegramId(999L);

        assertTrue(actualProfiles.isEmpty());
        verify(persons, times(1)).findByTelegramId(999L);
    }
}