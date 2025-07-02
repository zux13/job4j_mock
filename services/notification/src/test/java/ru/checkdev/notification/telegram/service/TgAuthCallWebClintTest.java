package ru.checkdev.notification.telegram.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.checkdev.notification.domain.AccountInfoDTO;
import ru.checkdev.notification.domain.PersonDTO;
import ru.checkdev.notification.service.Retry;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Testing TgAuthCallWebClint
 *
 * @author Dmitry Stepanov, user Dmitry
 * @since 06.10.2023
 */
@ExtendWith(MockitoExtension.class)
class TgAuthCallWebClintTest {
    private static final String URL = "http://tetsurl:15000";
    @Mock
    private WebClient webClientMock;
    @Mock
    private WebClient.RequestHeadersSpec requestHeadersMock;
    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriMock;
    @Mock
    private WebClient.RequestBodySpec requestBodyMock;
    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriMock;
    @Mock
    private WebClient.ResponseSpec responseMock;

    @Mock
    private ru.checkdev.notification.service.Retry retryMock;

    private TgAuthCallWebClient tgAuthCallWebClient;

    @BeforeEach
    void setUp() {
        tgAuthCallWebClient = new TgAuthCallWebClient(URL, retryMock);
        tgAuthCallWebClient.setWebClient(webClientMock);
    }


    @Test
    void whenDoGetThenReturnPersonDTO() {
        Integer personId = 100;
        var created = new Calendar.Builder()
                .set(Calendar.DAY_OF_MONTH, 23)
                .set(Calendar.MONTH, Calendar.OCTOBER)
                .set(Calendar.YEAR, 2023)
                .build();
        var personDto = new PersonDTO("mail", "password", true, Collections.EMPTY_LIST, created);
        when(retryMock.exec(any(Retry.Act.class), eq(null))).thenReturn(personDto);
        PersonDTO actual = tgAuthCallWebClient.doGet("/person/" + personId).block();
        assertThat(actual).isEqualTo(personDto);
    }

    @Test
    void whenDoGetThenReturnExceptionError() {
        Integer personId = 100;
        when(retryMock.exec(any(Retry.Act.class), eq(null))).thenReturn(null);
        PersonDTO actual = tgAuthCallWebClient.doGet("/person/" + personId).block();
        assertThat(actual).isNull();
    }

    @Test
    void whenDoPostSavePersonThenReturnNewPerson() {
        var created = new Calendar.Builder()
                .set(Calendar.DAY_OF_MONTH, 23)
                .set(Calendar.MONTH, Calendar.OCTOBER)
                .set(Calendar.YEAR, 2023)
                .build();
        var personDto = new PersonDTO("mail", "password", true, null, created);
        when(retryMock.exec(any(Retry.Act.class), eq(null))).thenReturn(personDto);
        Mono<Object> objectMono = tgAuthCallWebClient.doPost("/person/created", personDto);
        PersonDTO actual = (PersonDTO) objectMono.block();
        assertThat(actual).isEqualTo(personDto);
    }

    @Test
    void whenDoGetListThenReturnListOfAccountInfoDTO() {
        List<AccountInfoDTO> accounts = List.of(new AccountInfoDTO("test@example.com", "testuser"));
        when(retryMock.exec(any(Retry.Act.class), eq(null))).thenReturn(accounts);

        List<AccountInfoDTO> actual = tgAuthCallWebClient.doGetList("/accounts").block();
        assertThat(actual).isEqualTo(accounts);
    }

    @Test
    void whenDoGetListThenReturnExceptionError() {
        when(retryMock.exec(any(Retry.Act.class), eq(null))).thenReturn(null);
        List<AccountInfoDTO> actual = tgAuthCallWebClient.doGetList("/accounts").block();
        assertThat(actual).isNull();
    }

    @Test
    void fallbackGetReturnsEmptyMono() {
        Mono<PersonDTO> result = tgAuthCallWebClient.fallbackGet("/test", new RuntimeException("Test Exception"));
        assertTrue(result.blockOptional().isEmpty());
    }

    @Test
    void fallbackPostReturnsEmptyMono() {
        Mono<Object> result = tgAuthCallWebClient.fallbackPost("/test", new Object(), new RuntimeException("Test Exception"));
        assertTrue(result.blockOptional().isEmpty());
    }

    @Test
    void fallbackGetListReturnsEmptyMono() {
        Mono<List<AccountInfoDTO>> result = tgAuthCallWebClient.fallbackGetList("/test", new RuntimeException("Test Exception"));
        assertTrue(result.blockOptional().isEmpty());
    }
}