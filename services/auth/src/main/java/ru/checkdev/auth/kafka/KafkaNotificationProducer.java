package ru.checkdev.auth.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.checkdev.auth.domain.Notify;

@Service
@RequiredArgsConstructor
public class KafkaNotificationProducer {

    private final KafkaTemplate<String, String> kafka;
    private final ObjectMapper mapper = new ObjectMapper();

    public void send(Notify notify) {
        send("notifications", notify);
    }

    private <T> void send(String topic, T payload) {
        try {
            String json = mapper.writeValueAsString(payload);
            kafka.send(topic, json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Ошибка сериализации при отправке в Kafka", e);
        }
    }
}
