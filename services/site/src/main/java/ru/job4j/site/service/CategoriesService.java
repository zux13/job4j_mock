package ru.job4j.site.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.job4j.site.dto.CategoryDTO;
import ru.job4j.site.dto.InterviewDTO;
import ru.job4j.site.dto.TopicDTO;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CategoriesService {

    private final TopicsService topicsService;
    private final InterviewsService interviewsService;
    private final ObjectMapper mapper = new ObjectMapper();

    private final String baseUrl;

    public CategoriesService(TopicsService topicsService,
                             InterviewsService interviewsService,
                             @Value("${server.desc}") String baseUrl) {
        this.topicsService = topicsService;
        this.interviewsService = interviewsService;
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    public List<CategoryDTO> getAll() throws JsonProcessingException {
        var text = new RestAuthCall(baseUrl + "/categories/").get();
        return mapper.readValue(text, new TypeReference<>() {});
    }

    public List<CategoryDTO> getPopularFromDesc() throws JsonProcessingException {
        var text = new RestAuthCall(baseUrl + "/categories/most_pop").get();
        return mapper.readValue(text, new TypeReference<>() {});
    }

    public CategoryDTO create(String token, CategoryDTO category) throws JsonProcessingException {
        var out = new RestAuthCall(baseUrl + "/category/").post(
                token,
                mapper.writeValueAsString(category)
        );
        return mapper.readValue(out, CategoryDTO.class);
    }

    public void update(String token, CategoryDTO category) throws JsonProcessingException {
        new RestAuthCall(baseUrl + "/category/").put(
                token,
                mapper.writeValueAsString(category)
        );
    }

    public void updateStatistic(String token, int categoryId) throws JsonProcessingException {
        new RestAuthCall(baseUrl + "/category/statistic").put(
                token,
                mapper.writeValueAsString(categoryId)
        );
    }

    public List<CategoryDTO> getAllWithTopics() throws JsonProcessingException {
        var categoriesDTO = getAll();
        for (var categoryDTO : categoriesDTO) {
            categoryDTO.setTopicsSize(topicsService.getByCategory(categoryDTO.getId()).size());
        }
        return categoriesDTO;
    }

    public List<CategoryDTO> getMostPopular() throws JsonProcessingException {
        var categoriesDTO = getPopularFromDesc();
        List<InterviewDTO> newInterviews = interviewsService.getByType(1);
        Map<Integer, Integer> newInterviewByTopicId = newInterviews.stream()
                .collect(Collectors.groupingBy(
                        InterviewDTO::getTopicId,
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));

        for (var categoryDTO : categoriesDTO) {
            List<TopicDTO> topics = topicsService.getByCategory(categoryDTO.getId());
            categoryDTO.setTopicsSize(topics.size());
            int count = topics.stream()
                    .mapToInt(topicDTO -> newInterviewByTopicId.getOrDefault(topicDTO.getId(), 0))
                    .sum();
            categoryDTO.setNewInterviewsCount(count);
        }
        return categoriesDTO;
    }

    public String getNameById(List<CategoryDTO> list, int id) {
        for (CategoryDTO category : list) {
            if (id == category.getId()) {
                return category.getName();
            }
        }
        return "";
    }
}
