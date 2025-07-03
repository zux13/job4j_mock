package ru.job4j.site.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.job4j.site.dto.SubscribeCategory;
import ru.job4j.site.dto.SubscribeTopicDTO;
import ru.job4j.site.dto.UserDTO;
import ru.job4j.site.dto.UserTopicDTO;
import ru.job4j.site.kafka.KafkaNotificationProducer;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final KafkaNotificationProducer kafka;

    public void addSubscribeCategory(String token, int userId, int categoryId) {
        kafka.addCategory(new SubscribeCategory(userId, categoryId));
    }

    public void deleteSubscribeCategory(String token, int userId, int categoryId) {
        kafka.deleteCategory(new SubscribeCategory(userId, categoryId));
    }

    public void addSubscribeTopic(String token, int userId, int topicId) {
        kafka.addTopic(new SubscribeTopicDTO(userId, topicId));
    }

    public void deleteSubscribeTopic(String token, int userId, int topicId) {
        kafka.deleteTopic(new SubscribeTopicDTO(userId, topicId));
    }

    public UserDTO findCategoriesByUserId(int id) throws JsonProcessingException {
        var text = new RestAuthCall("http://localhost:9920/subscribeCategory/" + id).get();
        var mapper = new ObjectMapper();
        List<Integer> list = mapper.readValue(text, new TypeReference<>() {
        });
        return new UserDTO(id, list);
    }

    public UserTopicDTO findTopicByUserId(int id) throws JsonProcessingException {
        var text = new RestAuthCall("http://localhost:9920/subscribeTopic/" + id).get();
        var mapper = new ObjectMapper();
        List<Integer> list = mapper.readValue(text, new TypeReference<>() {
        });
        return new UserTopicDTO(id, list);
    }
}