package ru.job4j.site.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.job4j.site.dto.UserInfoDTO;

import java.util.Map;

@Service
@Slf4j
public class AuthService {

    @Value("${server.auth}")
    private String baseUrl;

    @Value("${security.oauth2.resource.userInfoUri}")
    private String userInfoUri;

    @Value("${security.oauth2.tokenUri}")
    private String oauth2Token;

    @Value("${server.auth.ping}")
    private String authServicePing;

    private final ObjectMapper mapper = new ObjectMapper();

    public UserInfoDTO userInfo(String token) throws JsonProcessingException {
        String url = userInfoUri.startsWith("http") ? userInfoUri : baseUrl + userInfoUri;
        String response = new RestAuthCall(url).get(token);
        return mapper.readValue(response, UserInfoDTO.class);
    }

    public String token(Map<String, String> params) throws JsonProcessingException {
        String result = "";
        try {
            String response = new RestAuthCall(oauth2Token).token(params);
            result = mapper.readTree(response).get("access_token").asText();
        } catch (Exception e) {
            log.error("Get token from service Auth error: {}", e.getMessage());
        }
        return result;
    }

    /**
     * Метод проверяет доступность сервера Auth.
     *
     * @return boolean true если доступен
     */
    public boolean getPing() {
        try {
            return !new RestAuthCall(authServicePing).get().isEmpty();
        } catch (Exception e) {
            log.error("Get PING from API Auth error: {}", e.getMessage());
            return false;
        }
    }

}
