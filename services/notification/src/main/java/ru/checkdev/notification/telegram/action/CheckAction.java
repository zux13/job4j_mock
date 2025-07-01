package ru.checkdev.notification.telegram.action;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.checkdev.notification.domain.AccountInfoDTO;
import ru.checkdev.notification.telegram.config.TgConfig;
import ru.checkdev.notification.telegram.service.TgAuthCallWebClient;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Slf4j
public class CheckAction implements Action {
    private final TgConfig tgConfig = new TgConfig("tg/", 8);
    private final TgAuthCallWebClient authCallWebClint;
    private final String urlSiteAuth;

    @Override
    public BotApiMethod<Message> handle(Message message) {
        var chatId = message.getChatId().toString();
        var telegramId = message.getFrom().getId();
        var sl = System.lineSeparator();
        var text = "";
        List<AccountInfoDTO> accounts;
        try {
            accounts = authCallWebClint.doGetList("/person/by-telegram/" + telegramId)
                    .block();
        } catch (Exception e) {
            log.error("WebClient doGetList error: {}", e.getMessage());
            text = "Сервис не доступен попробуйте позже" + sl
                    + "/start";
            return new SendMessage(chatId, text);
        }
        if (accounts == null || accounts.isEmpty()) {
            text = "К вашему telegram-аккаунту не привязано ни одного аккаунта.";
        } else {
            StringBuilder sb = new StringBuilder("Привязанные аккаунты:" + sl);
            for (AccountInfoDTO account : accounts) {
                sb.append("---").append(sl);
                sb.append("Email: ").append(account.getEmail()).append(sl);
                sb.append("Логин: ").append(account.getUsername()).append(sl);
            }
            text = sb.toString();
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
