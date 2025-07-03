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
import ru.checkdev.notification.domain.BindDTO;
import ru.checkdev.notification.telegram.service.TgAuthCallWebClient;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class BindActionTest {

    @Mock
    private TgAuthCallWebClient authCallWebClient;

    private BindAction bindAction;
    private Map<String, String> bindingBy;
    private Message message;
    private Chat chat;
    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        bindAction = new BindAction(authCallWebClient);
        bindingBy = new HashMap<>();
        message = new Message();
        chat = new Chat();
        user = new User();
        chat.setId(12345L);
        user.setId(67890L);
        message.setChat(chat);
        message.setFrom(user);
    }

    @Test
    void handleReturnsSendMessageWithEmailPrompt() {
        SendMessage sendMessage = (SendMessage) bindAction.handle(message);
        assertEquals("Введите email для привязки:", sendMessage.getText());
        assertEquals("12345", sendMessage.getChatId());
    }

    @Test
    void callbackInvalidEmailReturnsErrorMessage() {
        message.setText("invalid-email");
        SendMessage sendMessage = (SendMessage) bindAction.callback(message, bindingBy);
        String expectedText = "Email: invalid-email не корректный." + System.lineSeparator()
                + "попробуйте снова." + System.lineSeparator()
                + "/bind";
        assertEquals(expectedText, sendMessage.getText());
        assertEquals("12345", sendMessage.getChatId());
    }

    @Test
    void callbackValidEmailPromptsForPassword() {
        message.setText("test@example.com");
        SendMessage sendMessage = (SendMessage) bindAction.callback(message, bindingBy);
        assertEquals("Введите пароль:", sendMessage.getText());
        assertEquals("12345", sendMessage.getChatId());
    }

    @Test
    void callbackValidPasswordBindsAccountSuccessfully() {
        message.setText("test@example.com");
        bindAction.callback(message, bindingBy);
        message.setText("password123");

        Map<String, String> successResponse = new HashMap<>();
        successResponse.put("status", "success");
        when(authCallWebClient.doPost(eq("/bind"), any(BindDTO.class))).thenReturn(Mono.just(successResponse));

        SendMessage sendMessage = (SendMessage) bindAction.callback(message, bindingBy);
        assertEquals("Ваш аккаунт успешно привязан.", sendMessage.getText());
        assertEquals("12345", sendMessage.getChatId());
    }

    @Test
    void callbackAuthServiceUnavailableReturnsErrorMessage() {
        message.setText("test@example.com");
        bindAction.callback(message, bindingBy);
        message.setText("password123");

        when(authCallWebClient.doPost(eq("/bind"), any(BindDTO.class))).thenReturn(Mono.error(new RuntimeException("Service Down")));

        SendMessage sendMessage = (SendMessage) bindAction.callback(message, bindingBy);
        String expectedText = "Сервис не доступен попробуйте позже" + System.lineSeparator()
                + "/start";
        assertEquals(expectedText, sendMessage.getText());
        assertEquals("12345", sendMessage.getChatId());
    }

    @Test
    void callbackAuthServiceReturnsErrorObject() {
        message.setText("test@example.com");
        bindAction.callback(message, bindingBy);
        message.setText("password123");

        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Invalid credentials");
        when(authCallWebClient.doPost(eq("/bind"), any(BindDTO.class))).thenReturn(Mono.just(errorResponse));

        SendMessage sendMessage = (SendMessage) bindAction.callback(message, bindingBy);
        assertEquals("Ошибка привязки: Invalid credentials", sendMessage.getText());
        assertEquals("12345", sendMessage.getChatId());
    }
}