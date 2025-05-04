package ru.rsreu.videohosting.service;

import org.springframework.http.*;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.stereotype.Service;
import ru.rsreu.videohosting.dto.ViewRequestDTO;

import javax.servlet.http.HttpServletRequest;

@Service
public class CustomWebClientService {

    private final WebClient webClient;

    public CustomWebClientService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8082").build(); //DEBUG
    }

    public String sendPostRequestUpdateVideoViews(ViewRequestDTO viewRequestDTO,
                                                  CsrfToken token) {
        return webClient.post()
                .uri("/api/video/view")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(token.getHeaderName(), token.getToken())
                .bodyValue(viewRequestDTO)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
