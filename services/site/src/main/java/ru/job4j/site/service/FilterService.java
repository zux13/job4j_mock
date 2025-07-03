package ru.job4j.site.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.job4j.site.dto.FilterDTO;

@Service
public class FilterService {

    private final String baseUrl;
    private final ObjectMapper mapper = new ObjectMapper();

    public FilterService(@Value("${service.mock}") String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    public FilterDTO save(String token, FilterDTO filter) throws JsonProcessingException {
        var out = new RestAuthCall(baseUrl + "/filter/").post(
                token,
                mapper.writeValueAsString(filter)
        );
        return mapper.readValue(out, FilterDTO.class);
    }

    public FilterDTO getByUserId(String token, int userId) throws JsonProcessingException {
        var text = new RestAuthCall(baseUrl + "/filter/" + userId).get(token);
        return mapper.readValue(text, new TypeReference<>() {});
    }

    public void deleteByUserId(String token, int userId) throws JsonProcessingException {
        new RestAuthCall(baseUrl + "/filter/delete/" + userId).delete(
                token,
                mapper.writeValueAsString(userId)
        );
    }
}
