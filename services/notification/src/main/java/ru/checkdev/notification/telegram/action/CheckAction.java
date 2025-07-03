package ru.checkdev.notification.telegram.action;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.checkdev.notification.domain.AccountInfoDTO;
import ru.checkdev.notification.telegram.service.TgAuthCallWebClient;

import java.util.Map;

@AllArgsConstructor
@Slf4j
public class CheckAction implements Action {

    private final TgAuthCallWebClient authCallWebClint;

    @Override
    public BotApiMethod<Message> handle(Message message) {
        var chatId = message.getChatId().toString();
        var telegramId = message.getFrom().getId();
        var sl = System.lineSeparator();
        var text = "";

        AccountInfoDTO account;
        try {
            account = authCallWebClint.doGetByTelegram("/person/by-telegram/" + telegramId)
                    .block();
        } catch (Exception e) {
            log.error("WebClient doGetByTelegram error: {}", e.getMessage());
            text = "Сервис недоступен, попробуйте позже." + sl + "/start";
            return new SendMessage(chatId, text);
        }

        if (account == null) {
            text = "К вашему telegram-аккаунту не привязан ни один аккаунт.";
        } else {
            text = "Привязанный аккаунт:" + sl
                    + "Email: " + account.getEmail() + sl
                    + "Логин: " + account.getUsername();
        }

        return new SendMessage(chatId, text);
    }

    @Override
    public BotApiMethod<Message> callback(Message message, Map<String, String> bindingBy) {
        var chatId = message.getChatId().toString();
        bindingBy.remove(chatId);
        return handle(message);
    }
}
