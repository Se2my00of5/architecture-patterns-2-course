package ru.hits.core_service.integration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserServiceClient {

    private final RestClient.Builder restClientBuilder;

    @Value("${integration.user-service.base-url}")
    private String userServiceBaseUrl;

    @Value("${integration.user-service.oauth.client-id}")
    private String clientId;

    @Value("${integration.user-service.oauth.client-secret}")
    private String clientSecret;

    private volatile String cachedAccessToken;
    private volatile Instant accessTokenExpiresAt = Instant.EPOCH;

    public boolean userExists(UUID userId) {
        String accessToken = getServiceAccessToken();
        RestClient restClient = restClientBuilder.baseUrl(userServiceBaseUrl).build();

        try {
            restClient.get()
                    .uri("/api/users/{userId}", userId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (HttpClientErrorException.NotFound notFound) {
            return false;
        }
    }

    private synchronized String getServiceAccessToken() {
        if (cachedAccessToken != null && Instant.now().isBefore(accessTokenExpiresAt)) {
            return cachedAccessToken;
        }

        // создание запроса на получение токена по client_credentials
        RestClient restClient = restClientBuilder.baseUrl(userServiceBaseUrl).build();

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("scope", "api");

        Map<String, Object> tokenResponse = restClient.post()
                .uri("/oauth2/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(body)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });

        if (tokenResponse == null || tokenResponse.get("access_token") == null) {
            throw new IllegalStateException("Не удалось получить service access token");
        }


        //кэширование токена с запасом в 30 секунд до истечения срока действия
        cachedAccessToken = tokenResponse.get("access_token").toString();

        Number expiresIn = tokenResponse.get("expires_in") instanceof Number number
                ? number
                : 300;
        accessTokenExpiresAt = Instant.now().plusSeconds(Math.max(30, expiresIn.longValue() - 30));

        log.debug("Получен service access token");
        return cachedAccessToken;
    }
}
