package ru.job4j.site.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.job4j.site.dto.SubscribeCategory;
import ru.job4j.site.dto.SubscribeTopicDTO;

@Service
@RequiredArgsConstructor
public class KafkaNotificationProducer {

    private final KafkaTemplate<String, String> kafka;
    private final ObjectMapper mapper = new ObjectMapper();

    public void addCategory(SubscribeCategory sc) {
        send("subscribe-category-add", sc);
    }

    public void deleteCategory(SubscribeCategory sc) {
        send("subscribe-category-delete", sc);
    }

    public void addTopic(SubscribeTopicDTO st) {
        send("subscribe-topic-add", st);
    }

    public void deleteTopic(SubscribeTopicDTO st) {
        send("subscribe-topic-delete", st);
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
