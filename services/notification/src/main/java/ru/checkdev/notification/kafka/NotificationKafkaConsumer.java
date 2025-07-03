package ru.checkdev.notification.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import ru.checkdev.notification.domain.Notify;
import ru.checkdev.notification.service.NotificationService;

@Service
@AllArgsConstructor
@Slf4j
public class NotificationKafkaConsumer {

    private final ObjectMapper mapper = new ObjectMapper();
    private final NotificationService notificationService;

    @KafkaListener(topics = "notifications", groupId = "notification-group")
    public void listen(String message) {
        try {
            Notify notify = mapper.readValue(message, Notify.class);
            log.info("[Notifications] Получено сообщение: {}", notify);
            notificationService.put(notify);
        } catch (Exception e) {
            log.error("Ошибка десериализации сообщения из Kafka", e);
        }
    }
}