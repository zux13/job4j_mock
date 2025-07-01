package ru.checkdev.auth.repository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;
import ru.checkdev.auth.domain.Profile;
import ru.checkdev.auth.dto.ProfileDTO;

import javax.persistence.EntityManager;
import java.util.Collections;
import java.util.List;
import java.util.Calendar;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

/**
 * CheckDev пробное собеседование
 * Тест на класс PersonRepository
 *
 * @author Dmitry Stepanov
 * @version 22.09.2023'T'21:14
 */
@RunWith(SpringRunner.class)
@DataJpaTest()
public class PersonRepositoryTest {
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private PersonRepository personRepository;

    @Before
    public void clearTable() {
        entityManager.createQuery("delete from profile").executeUpdate();
    }

    @Test
    public void injectedComponentAreNotNull() {
        assertNotNull(entityManager);
        assertNotNull(personRepository);
    }

    @Test
    public void whenFindProfileByIdThenReturnNull() {
        ProfileDTO profileDTO = personRepository.findProfileById(-1);
        assertNull(profileDTO);
    }

    @Test
    public void whenFindProfileOrderByCreatedDescThenReturnEmptyList() {
        var listProfileDTO = personRepository.findProfileOrderByCreatedDesc();
        assertThat(listProfileDTO, is(Collections.emptyList()));
    }

    @Test
    public void whenFindByEmailThenReturnProfile() {
        Profile profile = new Profile("testuser", "test@example.com", "password");
        profile.setCreated(Calendar.getInstance());
        profile.setUpdated(Calendar.getInstance());
        entityManager.persist(profile);
        entityManager.flush();

        Profile found = personRepository.findByEmail("test@example.com");
        assertNotNull(found);
        assertEquals("test@example.com", found.getEmail());
    }

    @Test
    public void whenFindByEmailThenReturnNull() {
        Profile found = personRepository.findByEmail("nonexistent@example.com");
        assertNull(found);
    }

    @Test
    public void whenFindByEmailAndUsernameThenReturnProfile() {
        Profile profile = new Profile("testuser", "test@example.com", "password");
        profile.setCreated(Calendar.getInstance());
        profile.setUpdated(Calendar.getInstance());
        entityManager.persist(profile);
        entityManager.flush();

        Profile found = personRepository.findByEmailAndUsername("test@example.com", "testuser");
        assertNotNull(found);
        assertEquals("test@example.com", found.getEmail());
        assertEquals("testuser", found.getUsername());
    }

    @Test
    public void whenFindByEmailAndUsernameThenReturnNull() {
        Profile found = personRepository.findByEmailAndUsername("nonexistent@example.com", "nonexistent");
        assertNull(found);
    }

    @Test
    public void whenFindByKeyThenReturnProfile() {
        Profile profile = new Profile("testuser", "test@example.com", "password");
        profile.setKey("testkey");
        profile.setCreated(Calendar.getInstance());
        profile.setUpdated(Calendar.getInstance());
        entityManager.persist(profile);
        entityManager.flush();

        Profile found = personRepository.findByKey("testkey");
        assertNotNull(found);
        assertEquals("testkey", found.getKey());
    }

    @Test
    public void whenFindByKeyThenReturnNull() {
        Profile found = personRepository.findByKey("nonexistentkey");
        assertNull(found);
    }

    @Test
    public void whenFindByTelegramIdThenReturnListOfProfiles() {
        Profile profile1 = new Profile("user1", "user1@example.com", "pass1");
        profile1.setTelegramId(123L);
        profile1.setCreated(Calendar.getInstance());
        profile1.setUpdated(Calendar.getInstance());
        entityManager.persist(profile1);

        Profile profile2 = new Profile("user2", "user2@example.com", "pass2");
        profile2.setTelegramId(123L);
        profile2.setCreated(Calendar.getInstance());
        profile2.setUpdated(Calendar.getInstance());
        entityManager.persist(profile2);
        entityManager.flush();

        List<Profile> found = personRepository.findByTelegramId(123L);
        assertNotNull(found);
        assertEquals(2, found.size());
        assertTrue(found.stream().anyMatch(p -> "user1@example.com".equals(p.getEmail())));
        assertTrue(found.stream().anyMatch(p -> "user2@example.com".equals(p.getEmail())));
    }

    @Test
    public void whenFindByTelegramIdThenReturnEmptyList() {
        List<Profile> found = personRepository.findByTelegramId(999L);
        assertNotNull(found);
        assertTrue(found.isEmpty());
    }
}
