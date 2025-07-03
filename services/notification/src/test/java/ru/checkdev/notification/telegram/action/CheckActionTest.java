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

import java.util.*;

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
        checkAction = new CheckAction(authCallWebClient);
        message = new Message();
        chat = new Chat();
        user = new User();
        chat.setId(12345L);
        user.setId(67890L);
        message.setChat(chat);
        message.setFrom(user);
    }

    @Test
    void handleReturnsNoAccountsMessageWhenNoAccountsFound() {
        when(authCallWebClient.doGetByTelegram(anyString())).thenReturn(Mono.empty());

        SendMessage sendMessage = (SendMessage) checkAction.handle(message);
        assertEquals("К вашему telegram-аккаунту не привязан ни один аккаунт.", sendMessage.getText());
        assertEquals("12345", sendMessage.getChatId());
    }

    @Test
    void handleReturnsAccountInfoWhenListIsNotEmpty() {
        AccountInfoDTO account = new AccountInfoDTO("test1@example.com", "user1");

        when(authCallWebClient.doGetByTelegram(anyString())).thenReturn(Mono.just(account));

        SendMessage sendMessage = (SendMessage) checkAction.handle(message);
        String expectedText = "Привязанный аккаунт:" + System.lineSeparator()
                + "Email: test1@example.com" + System.lineSeparator()
                + "Логин: user1";
        assertEquals(expectedText, sendMessage.getText());
        assertEquals("12345", sendMessage.getChatId());
    }

    @Test
    void handleReturnsServiceUnavailableMessageOnError() {
        when(authCallWebClient.doGetByTelegram(anyString())).thenReturn(Mono.error(new RuntimeException("Service Down")));

        SendMessage sendMessage = (SendMessage) checkAction.handle(message);
        String expectedText = "Сервис недоступен, попробуйте позже." + System.lineSeparator() + "/start";
        assertEquals(expectedText, sendMessage.getText());
        assertEquals("12345", sendMessage.getChatId());
    }

    @Test
    void callbackCallsHandleMethod() {
        when(authCallWebClient.doGetByTelegram(anyString())).thenReturn(Mono.empty());
        Map<String, String> bindingBy = new HashMap<>();
        bindingBy.put("12345", "someValue");

        SendMessage sendMessage = (SendMessage) checkAction.callback(message, bindingBy);
        assertEquals("К вашему telegram-аккаунту не привязан ни один аккаунт.", sendMessage.getText());
        assertEquals("12345", sendMessage.getChatId());
        assertEquals(0, bindingBy.size());
    }
}