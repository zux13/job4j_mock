package ru.checkdev.notification.telegram.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InfoActionTest {

    private InfoAction infoAction;
    private Message message;
    private Chat chat;

    @BeforeEach
    void setUp() {
        List<String> actions = Arrays.asList("/start", "/info", "/bind", "/check", "/new", "/unbind");
        infoAction = new InfoAction(actions);
        message = new Message();
        chat = new Chat();
        chat.setId(12345L);
        message.setChat(chat);
    }

    @Test
    void handleReturnsSendMessageWithAvailableCommands() {
        SendMessage sendMessage = (SendMessage) infoAction.handle(message);
        String expectedText = "Выберите действие:" + System.lineSeparator()
                + "/start" + System.lineSeparator()
                + "/info" + System.lineSeparator()
                + "/bind" + System.lineSeparator()
                + "/check" + System.lineSeparator()
                + "/new" + System.lineSeparator()
                + "/unbind" + System.lineSeparator();
        assertEquals(expectedText, sendMessage.getText());
        assertEquals("12345", sendMessage.getChatId());
    }

    @Test
    void handleReturnsSendMessageWithEmptyCommandsWhenListIsEmpty() {
        infoAction = new InfoAction(Collections.emptyList());
        SendMessage sendMessage = (SendMessage) infoAction.handle(message);
        String expectedText = "Выберите действие:" + System.lineSeparator();
        assertEquals(expectedText, sendMessage.getText());
        assertEquals("12345", sendMessage.getChatId());
    }

    @Test
    void callbackCallsHandleMethodAndRemovesFromBindingBy() {
        Map<String, String> bindingBy = new HashMap<>();
        bindingBy.put("12345", "someValue");

        SendMessage sendMessage = (SendMessage) infoAction.callback(message, bindingBy);
        String expectedText = "Выберите действие:" + System.lineSeparator()
                + "/start" + System.lineSeparator()
                + "/info" + System.lineSeparator()
                + "/bind" + System.lineSeparator()
                + "/check" + System.lineSeparator()
                + "/new" + System.lineSeparator()
                + "/unbind" + System.lineSeparator();
        assertEquals(expectedText, sendMessage.getText());
        assertEquals("12345", sendMessage.getChatId());
        assertEquals(0, bindingBy.size());
    }
}