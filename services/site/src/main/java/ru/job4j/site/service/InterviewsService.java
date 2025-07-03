package ru.job4j.site.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import ru.job4j.site.dto.InterviewDTO;
import ru.job4j.site.util.RestPageImpl;

import java.util.List;

@Service
public class InterviewsService {

    private final String baseUrl;
    private final ObjectMapper mapper;

    public InterviewsService(@Value("${service.mock}") String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public Page<InterviewDTO> getAll(String token, int page, int size) throws JsonProcessingException {
        String url = String.format("%s/interviews/?page=%d&size=%d", baseUrl, page, size);
        String text = new RestAuthCall(url).get(token);
        var pageType = mapper.getTypeFactory().constructParametricType(RestPageImpl.class, InterviewDTO.class);
        return mapper.readValue(text, pageType);
    }

    public List<InterviewDTO> getByType(int type) throws JsonProcessingException {
        String url = String.format("%s/interviews/%d", baseUrl, type);
        String text = new RestAuthCall(url).get();
        return mapper.readValue(text, new TypeReference<>() {});
    }

    public Page<InterviewDTO> getByTopicId(int topicId, int page, int size) throws JsonProcessingException {
        String url = String.format("%s/interviews/findByTopicId/%d?page=%d&size=%d", baseUrl, topicId, page, size);
        String text = new RestAuthCall(url).get();
        var pageType = mapper.getTypeFactory().constructParametricType(RestPageImpl.class, InterviewDTO.class);
        return mapper.readValue(text, pageType);
    }

    public Page<InterviewDTO> getByTopicsIds(List<Integer> topicIds, int page, int size) throws JsonProcessingException {
        String tids = parseIdsListToString(topicIds);
        String url = String.format("%s/interviews/findByTopicsIds/%s?page=%d&size=%d", baseUrl, tids, page, size);
        String text = new RestAuthCall(url).get();
        var pageType = mapper.getTypeFactory().constructParametricType(RestPageImpl.class, InterviewDTO.class);
        return mapper.readValue(text, pageType);
    }

    private String parseIdsListToString(List<Integer> list) {
        return String.join(",", list.stream().map(String::valueOf).toList());
    }
}
