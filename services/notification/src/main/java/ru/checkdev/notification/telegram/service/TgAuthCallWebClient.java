package ru.checkdev.notification.telegram.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.beans.factory.annotation.Value;
import reactor.core.publisher.Mono;
import ru.checkdev.notification.domain.AccountInfoDTO;
import ru.checkdev.notification.domain.PersonDTO;
import ru.checkdev.notification.service.CircuitBreaker;
import ru.checkdev.notification.service.Retry;

import java.util.List;

@Service
@Slf4j
public class TgAuthCallWebClient {
    private WebClient webClient;
    private final Retry retry;
    private final CircuitBreaker circuitBreaker;

    public TgAuthCallWebClient(@Value("${server.auth}") String urlAuth, Retry retry, CircuitBreaker circuitBreaker) {
        this.webClient = WebClient.create(urlAuth);
        this.retry = retry;
        this.circuitBreaker = circuitBreaker;
    }

    public void setWebClient(WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * Метод GET с применением Retry и Circuit Breaker
     *
     * @param url URL http
     * @return Mono<Person>
     */
    public Mono<PersonDTO> doGet(String url) {
        try {
            return Mono.justOrEmpty(circuitBreaker.exec(() -> retry.exec(
                    () -> webClient
                            .get()
                            .uri(url)
                            .retrieve()
                            .bodyToMono(PersonDTO.class)
                            .doOnError(err -> log.error("API not found: {}", err.getMessage()))
                            .block(),
                    null
            ), null));
        } catch (CircuitBreaker.CircuitBreakerOpenException e) {
            return fallbackGet(url, e);
        } catch (Exception e) {
            return fallbackGet(url, e);
        }
    }

    /**
     * Метод POST с применением Retry и Circuit Breaker
     *
     * @param url       URL http
     * @param dto       Body Object
     * @return Mono<Object>
     */
    public Mono<Object> doPost(String url, Object dto) {
        try {
            return Mono.justOrEmpty(circuitBreaker.exec(() -> retry.exec(
                    () -> webClient
                            .post()
                            .uri(url)
                            .bodyValue(dto)
                            .retrieve()
                            .bodyToMono(Object.class)
                            .doOnError(err -> log.error("API not found: {}", err.getMessage()))
                            .block(),
                    null
            ), null));
        } catch (CircuitBreaker.CircuitBreakerOpenException e) {
            return fallbackPost(url, dto, e);
        } catch (Exception e) {
            return fallbackPost(url, dto, e);
        }
    }

    // Fallback метод для GET
    public Mono<PersonDTO> fallbackGet(String url, Throwable throwable) {
        log.error("GET request failed, fallback triggered: {}", throwable.getMessage());
        return Mono.empty(); // Или возвращайте какой-то запасной ответ
    }

    // Fallback метод для POST
    public Mono<Object> fallbackPost(String url, Object dto, Throwable throwable) {
        log.error("POST request failed, fallback triggered: {}", throwable.getMessage());
        return Mono.empty(); // Или возвращайте какой-то запасной ответ
    }

    public Mono<List<AccountInfoDTO>> doGetList(String url) {
        try {
            return Mono.justOrEmpty(circuitBreaker.exec(() -> retry.exec(
                    () -> webClient
                            .get()
                            .uri(url)
                            .retrieve()
                            .bodyToMono(new ParameterizedTypeReference<List<AccountInfoDTO>>() {
                            })
                            .doOnError(err -> log.error("API not found: {}", err.getMessage()))
                            .block(),
                    null
            ), null));
        } catch (CircuitBreaker.CircuitBreakerOpenException e) {
            return fallbackGetList(url, e);
        } catch (Exception e) {
            return fallbackGetList(url, e);
        }
    }

    public Mono<List<AccountInfoDTO>> fallbackGetList(String url, Throwable throwable) {
        log.error("GET request failed, fallback triggered: {}", throwable.getMessage());
        return Mono.empty();
    }
}
