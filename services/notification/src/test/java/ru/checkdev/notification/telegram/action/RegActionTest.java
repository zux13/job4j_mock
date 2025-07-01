package ru.checkdev.notification.telegram.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import reactor.core.publisher.Mono;
import ru.checkdev.notification.domain.PersonDTO;
import ru.checkdev.notification.telegram.config.TgConfig;
import ru.checkdev.notification.telegram.service.TgAuthCallWebClient;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class RegActionTest {

    @Mock
    private TgAuthCallWebClient authCallWebClient;

    private RegAction regAction;
    private Map<String, String> bindingBy;
    private Message message;
    private Chat chat;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        regAction = new RegAction(authCallWebClient, "http://test.url");
        bindingBy = new HashMap<>();
        message = new Message();
        chat = new Chat();
        chat.setId(12345L);
        message.setChat(chat);
    }

    @Test
    void handleReturnsSendMessageWithEmailPrompt() {
        SendMessage sendMessage = (SendMessage) regAction.handle(message);
        assertEquals("Введите email для регистрации:", sendMessage.getText());
        assertEquals("12345", sendMessage.getChatId());
    }

    @Test
    void callbackInvalidEmailReturnsErrorMessage() {
        message.setText("invalid-email");
        SendMessage sendMessage = (SendMessage) regAction.callback(message, bindingBy);
        String expectedText = "Email: invalid-email не корректный.\n"
                + "попробуйте снова.\n"
                + "/new";
        assertEquals(expectedText, sendMessage.getText());
        assertEquals("12345", sendMessage.getChatId());
    }

    @Test
    void callbackAuthServiceUnavailableReturnsErrorMessage() {
        message.setText("test@example.com");

        when(authCallWebClient.doPost(eq("/registration"), any(PersonDTO.class))).thenReturn(Mono.error(new RuntimeException("Service Down")));

        SendMessage sendMessage = (SendMessage) regAction.callback(message, bindingBy);
        String expectedText = "Сервис не доступен попробуйте позже\n"
                + "/start";
        assertEquals(expectedText, sendMessage.getText());
        assertEquals("12345", sendMessage.getChatId());
    }

    @Test
    void callbackAuthServiceReturnsErrorObject() {
        message.setText("test@example.com");

        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "User already exists");
        when(authCallWebClient.doPost(eq("/registration"), any(PersonDTO.class))).thenReturn(Mono.just(errorResponse));

        SendMessage sendMessage = (SendMessage) regAction.callback(message, bindingBy);
        assertEquals("Ошибка регистрации: User already exists", sendMessage.getText());
        assertEquals("12345", sendMessage.getChatId());
    }
}