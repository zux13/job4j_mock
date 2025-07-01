package ru.checkdev.notification.telegram.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import reactor.core.publisher.Mono;
import ru.checkdev.notification.domain.AccountInfoDTO;
import ru.checkdev.notification.telegram.service.TgAuthCallWebClient;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class CheckActionTest {

    @Mock
    private TgAuthCallWebClient authCallWebClient;

    private CheckAction checkAction;
    private Message message;
    private Chat chat;
    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        checkAction = new CheckAction(authCallWebClient, "http://test.url");
        message = new Message();
        chat = new Chat();
        user = new User();
        chat.setId(12345L);
        user.setId(67890L);
        message.setChat(chat);
        message.setFrom(user);
    }

    @Test
    void handleReturnsNoAccountsMessageWhenListIsEmpty() {
        when(authCallWebClient.doGetList(anyString())).thenReturn(Mono.just(Collections.emptyList()));

        SendMessage sendMessage = (SendMessage) checkAction.handle(message);
        assertEquals("К вашему telegram-аккаунту не привязано ни одного аккаунта.", sendMessage.getText());
        assertEquals("12345", sendMessage.getChatId());
    }

    @Test
    void handleReturnsAccountInfoWhenListIsNotEmpty() {
        AccountInfoDTO account1 = new AccountInfoDTO("test1@example.com", "user1");
        AccountInfoDTO account2 = new AccountInfoDTO("test2@example.com", "user2");
        List<AccountInfoDTO> accounts = List.of(account1, account2);

        when(authCallWebClient.doGetList(anyString())).thenReturn(Mono.just(accounts));

        SendMessage sendMessage = (SendMessage) checkAction.handle(message);
        String expectedText = "Привязанные аккаунты:" + System.lineSeparator()
                + "---" + System.lineSeparator()
                + "Email: test1@example.com" + System.lineSeparator()
                + "Логин: user1" + System.lineSeparator()
                + "---" + System.lineSeparator()
                + "Email: test2@example.com" + System.lineSeparator()
                + "Логин: user2" + System.lineSeparator();
        assertEquals(expectedText, sendMessage.getText());
        assertEquals("12345", sendMessage.getChatId());
    }

    @Test
    void handleReturnsServiceUnavailableMessageOnError() {
        when(authCallWebClient.doGetList(anyString())).thenReturn(Mono.error(new RuntimeException("Service Down")));

        SendMessage sendMessage = (SendMessage) checkAction.handle(message);
        String expectedText = "Сервис не доступен попробуйте позже" + System.lineSeparator()
                + "/start";
        assertEquals(expectedText, sendMessage.getText());
        assertEquals("12345", sendMessage.getChatId());
    }

    @Test
    void callbackCallsHandleMethod() {
        when(authCallWebClient.doGetList(anyString())).thenReturn(Mono.just(Collections.emptyList()));
        Map<String, String> bindingBy = new HashMap<>();
        bindingBy.put("12345", "someValue");

        SendMessage sendMessage = (SendMessage) checkAction.callback(message, bindingBy);
        assertEquals("К вашему telegram-аккаунту не привязано ни одного аккаунта.", sendMessage.getText());
        assertEquals("12345", sendMessage.getChatId());
        assertEquals(0, bindingBy.size());
    }
}