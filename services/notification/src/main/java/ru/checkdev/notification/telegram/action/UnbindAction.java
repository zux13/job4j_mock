package ru.checkdev.notification.telegram.action;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.checkdev.notification.domain.UnbindDTO;
import ru.checkdev.notification.telegram.config.TgConfig;
import ru.checkdev.notification.telegram.service.TgAuthCallWebClient;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@AllArgsConstructor
@Slf4j
public class UnbindAction implements Action {
    private static final String ERROR_OBJECT = "error";
    private final Map<String, String> chatEmail = new ConcurrentHashMap<>();
    private final TgConfig tgConfig = new TgConfig("tg/", 8);
    private final String sl = System.lineSeparator();
    private final TgAuthCallWebClient authCallWebClint;
    private final String urlSiteAuth;

    @Override
    public BotApiMethod<Message> handle(Message message) {
        var chatId = message.getChatId().toString();
        chatEmail.remove(chatId);
        var text = "Введите email для отвязки:";
        return new SendMessage(chatId, text);
    }

    @Override
    public BotApiMethod<Message> callback(Message message, Map<String, String> bindingBy) {
        var chatId = message.getChatId().toString();
        var userMsg = message.getText();
        var text = "";

        if (!chatEmail.containsKey(chatId)) {
            if (!tgConfig.isEmail(userMsg)) {
                text = "Email: " + userMsg + " не корректный." + sl
                        + "попробуйте снова." + sl
                        + "/unbind";
                bindingBy.remove(chatId);
                return new SendMessage(chatId, text);
            }
            chatEmail.put(chatId, userMsg);
            text = "Введите пароль:";
            return new SendMessage(chatId, text);
        } else {
            var email = chatEmail.get(chatId);
            var password = userMsg;
            var unbindDTO = new UnbindDTO(email, password);
            Object result;
            try {
                result = authCallWebClint.doPost("/unbind", unbindDTO).block();
            } catch (Exception e) {
                log.error("WebClient doPost error: {}", e.getMessage());
                text = "Сервис не доступен попробуйте позже" + sl
                        + "/start";
                chatEmail.remove(chatId);
                bindingBy.remove(chatId);
                return new SendMessage(chatId, text);
            }

            var mapObject = tgConfig.getObjectToMap(result);

            if (mapObject.containsKey(ERROR_OBJECT)) {
                text = "Ошибка отвязки: " + mapObject.get(ERROR_OBJECT);
                chatEmail.remove(chatId);
                bindingBy.remove(chatId);
                return new SendMessage(chatId, text);
            }
            text = "Ваш аккаунт успешно отвязан.";
            chatEmail.remove(chatId);
            bindingBy.remove(chatId);
            return new SendMessage(chatId, text);
        }
    }
}
