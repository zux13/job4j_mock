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
import ru.checkdev.notification.domain.UnbindDTO;
import ru.checkdev.notification.telegram.service.TgAuthCallWebClient;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class UnbindActionTest {

    @Mock
    private TgAuthCallWebClient authCallWebClient;

    private UnbindAction unbindAction;
    private Map<String, String> bindingBy;
    private Message message;
    private Chat chat;
    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        unbindAction = new UnbindAction(authCallWebClient);
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
        SendMessage sendMessage = (SendMessage) unbindAction.handle(message);
        assertEquals("Введите email для отвязки:", sendMessage.getText());
        assertEquals("12345", sendMessage.getChatId());
    }

    @Test
    void callbackInvalidEmailReturnsErrorMessage() {
        message.setText("invalid-email");
        SendMessage sendMessage = (SendMessage) unbindAction.callback(message, bindingBy);
        String expectedText = "Email: invalid-email не корректный.\n"
                + "попробуйте снова.\n"
                + "/unbind";
        assertEquals(expectedText, sendMessage.getText());
        assertEquals("12345", sendMessage.getChatId());
    }

    @Test
    void callbackValidEmailPromptsForPassword() {
        message.setText("test@example.com");
        SendMessage sendMessage = (SendMessage) unbindAction.callback(message, bindingBy);
        assertEquals("Введите пароль:", sendMessage.getText());
        assertEquals("12345", sendMessage.getChatId());
    }

    @Test
    void callbackValidPasswordUnbindsAccountSuccessfully() {
        message.setText("test@example.com");
        unbindAction.callback(message, bindingBy);
        message.setText("password123");

        Map<String, String> successResponse = new HashMap<>();
        successResponse.put("status", "success");
        when(authCallWebClient.doPost(eq("/unbind"), any(UnbindDTO.class))).thenReturn(Mono.just(successResponse));

        SendMessage sendMessage = (SendMessage) unbindAction.callback(message, bindingBy);
        assertEquals("Ваш аккаунт успешно отвязан.", sendMessage.getText());
        assertEquals("12345", sendMessage.getChatId());
    }

    @Test
    void callbackAuthServiceUnavailableReturnsErrorMessage() {
        message.setText("test@example.com");
        unbindAction.callback(message, bindingBy);
        message.setText("password123");

        when(authCallWebClient.doPost(eq("/unbind"), any(UnbindDTO.class))).thenReturn(Mono.error(new RuntimeException("Service Down")));

        SendMessage sendMessage = (SendMessage) unbindAction.callback(message, bindingBy);
        String expectedText = "Сервис не доступен попробуйте позже" + System.lineSeparator()
                + "/start";
        assertEquals(expectedText, sendMessage.getText());
        assertEquals("12345", sendMessage.getChatId());
    }

    @Test
    void callbackAuthServiceReturnsErrorObject() {
        message.setText("test@example.com");
        unbindAction.callback(message, bindingBy);
        message.setText("password123");

        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Invalid credentials");
        when(authCallWebClient.doPost(eq("/unbind"), any(UnbindDTO.class))).thenReturn(Mono.just(errorResponse));

        SendMessage sendMessage = (SendMessage) unbindAction.callback(message, bindingBy);
        assertEquals("Ошибка отвязки: Invalid credentials", sendMessage.getText());
        assertEquals("12345", sendMessage.getChatId());
    }
}