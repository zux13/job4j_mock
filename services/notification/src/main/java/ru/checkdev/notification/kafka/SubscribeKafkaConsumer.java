package ru.checkdev.notification.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import ru.checkdev.notification.domain.SubscribeCategory;
import ru.checkdev.notification.domain.SubscribeTopic;
import ru.checkdev.notification.domain.SubscribeTopicDTO;
import ru.checkdev.notification.service.SubscribeCategoryService;
import ru.checkdev.notification.service.SubscribeTopicService;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscribeKafkaConsumer {

    private final ObjectMapper mapper = new ObjectMapper();
    private final SubscribeCategoryService categoryService;
    private final SubscribeTopicService topicService;

    @KafkaListener(topics = "subscribe-category-add", groupId = "notification-group")
    public void handleAddCategory(String message) {
        try {
            SubscribeCategory sc = mapper.readValue(message, SubscribeCategory.class);
            log.info("[Category ADD] Получено сообщение: {}", sc);
            categoryService.save(sc);
        } catch (Exception e) {
            log.error("Ошибка десериализации сообщения из Kafka", e);
        }
    }

    @KafkaListener(topics = "subscribe-category-delete", groupId = "notification-group")
    public void handleDeleteCategory(String message) {
        try {
            SubscribeCategory sc = mapper.readValue(message, SubscribeCategory.class);
            log.info("[Category DELETE] Получено сообщение: {}", sc);
            categoryService.delete(sc);
        } catch (Exception e) {
            log.error("Ошибка десериализации сообщения из Kafka", e);
        }
    }

    @KafkaListener(topics = "subscribe-topic-add", groupId = "notification-group")
    public void handleAddTopic(String message) {
        try {
            SubscribeTopicDTO dto = mapper.readValue(message, SubscribeTopicDTO.class);
            log.info("[Topic ADD] Получено сообщение: {}", dto);
            var entity = new SubscribeTopic(0, dto.getUserId(), dto.getTopicId());
            topicService.save(entity);
        } catch (Exception e) {
            log.error("Ошибка десериализации сообщения из Kafka", e);
        }
    }

    @KafkaListener(topics = "subscribe-topic-delete", groupId = "notification-group")
    public void handleDeleteTopic(String message) {
        try {
            SubscribeTopicDTO dto = mapper.readValue(message, SubscribeTopicDTO.class);
            log.info("[Topic DELETE] Получено сообщение: {}", dto);
            var entity = new SubscribeTopic(0, dto.getUserId(), dto.getTopicId());
            topicService.delete(entity);
        } catch (Exception e) {
            log.error("Ошибка десериализации сообщения из Kafka", e);
        }
    }
}

