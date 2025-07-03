package ru.job4j.site.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.job4j.site.dto.CategoryDTO;
import ru.job4j.site.dto.TopicDTO;
import ru.job4j.site.dto.TopicIdNameDTO;
import ru.job4j.site.dto.TopicLiteDTO;

import java.util.Calendar;
import java.util.List;

@Service
public class TopicsService {

    private final ObjectMapper mapper = new ObjectMapper();
    private final String baseUrl;

    public TopicsService(@Value("${server.desc}") String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    public List<TopicDTO> getByCategory(int id) throws JsonProcessingException {
        String url = baseUrl + "/topics/" + id;
        String text = new RestAuthCall(url).get();
        return mapper.readValue(text, new TypeReference<>() {});
    }

    public TopicDTO getById(int id) throws JsonProcessingException {
        String url = baseUrl + "/topic/" + id;
        String text = new RestAuthCall(url).get();
        return mapper.readValue(text, TopicDTO.class);
    }

    public TopicDTO create(String token, TopicLiteDTO topicLite) throws JsonProcessingException {
        TopicDTO topic = new TopicDTO();
        topic.setName(topicLite.getName());
        topic.setPosition(topicLite.getPosition());
        topic.setText(topicLite.getText());

        CategoryDTO category = new CategoryDTO();
        category.setId(topicLite.getCategoryId());
        topic.setCategory(category);

        String url = baseUrl + "/topic/";
        String json = mapper.writeValueAsString(topic);
        String out = new RestAuthCall(url).post(token, json);
        return mapper.readValue(out, TopicDTO.class);
    }

    public void update(String token, TopicDTO topic) throws JsonProcessingException {
        topic.setUpdated(Calendar.getInstance());
        String url = baseUrl + "/topic/";
        String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(topic);
        new RestAuthCall(url).update(token, json);
    }

    public void delete(String token, int id) throws JsonProcessingException {
        TopicDTO topic = new TopicDTO();
        topic.setId(id);
        String url = baseUrl + "/topic/";
        String json = mapper.writeValueAsString(topic);
        new RestAuthCall(url).delete(token, json);
    }

    public String getNameById(int id) {
        String url = String.format("%s/topic/name/%d", baseUrl, id);
        return new RestAuthCall(url).get();
    }

    public List<TopicIdNameDTO> getTopicIdNameDtoByCategory(int categoryId) throws JsonProcessingException {
        String url = String.format("%s/topics/getByCategoryId/%d", baseUrl, categoryId);
        String text = new RestAuthCall(url).get();
        return mapper.readValue(text, new TypeReference<>() {});
    }
}
