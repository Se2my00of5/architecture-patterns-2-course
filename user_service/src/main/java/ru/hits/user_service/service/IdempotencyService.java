package ru.hits.user_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.hits.user_service.entity.IdempotencyRecordEntity;
import ru.hits.user_service.exception.IdempotencyConflictException;
import ru.hits.user_service.repository.IdempotencyRecordRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
@Slf4j
public class IdempotencyService {

    private final IdempotencyRecordRepository idempotencyRecordRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public <T> T execute(
            String scope,
            String idempotencyKey,
            String httpMethod,
            String requestPath,
            Object requestBody,
            Class<T> responseType,
            Supplier<T> action
    ) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IdempotencyConflictException("Отсутствует заголовок Idempotency-Key");
        }

        String requestHash = buildRequestHash(requestBody);

        var existing = idempotencyRecordRepository
                .findByIdempotencyKeyAndScopeAndHttpMethodAndRequestPath(idempotencyKey, scope, httpMethod, requestPath);

        if (existing.isPresent()) {
            var record = existing.get();
            if (!record.getRequestHash().equals(requestHash)) {
                throw new IdempotencyConflictException(
                        "Idempotency-Key уже использован для другого содержимого запроса"
                );
            }
            return deserializeResponse(record.getResponseBody(), responseType);
        }

        T response = action.get();

        IdempotencyRecordEntity record = IdempotencyRecordEntity.builder()
                .idempotencyKey(idempotencyKey)
                .scope(scope)
                .httpMethod(httpMethod)
                .requestPath(requestPath)
                .requestHash(requestHash)
                .responseBody(serializeResponse(response))
                .createdAt(LocalDateTime.now())
                .build();

        try {
            idempotencyRecordRepository.save(record);
        } catch (DataIntegrityViolationException ex) {
            log.debug("Idempotency race detected for key={}, scope={}", idempotencyKey, scope);
            var afterRace = idempotencyRecordRepository
                    .findByIdempotencyKeyAndScopeAndHttpMethodAndRequestPath(idempotencyKey, scope, httpMethod, requestPath)
                    .orElseThrow(() -> ex);

            if (!afterRace.getRequestHash().equals(requestHash)) {
                throw new IdempotencyConflictException(
                        "Idempotency-Key уже использован для другого содержимого запроса"
                );
            }
            return deserializeResponse(afterRace.getResponseBody(), responseType);
        }

        return response;
    }

    @Transactional
    public void executeVoid(
            String scope,
            String idempotencyKey,
            String httpMethod,
            String requestPath,
            Object requestBody,
            Runnable action
    ) {
        execute(scope, idempotencyKey, httpMethod, requestPath, requestBody, Void.class, () -> {
            action.run();
            return null;
        });
    }

    private String buildRequestHash(Object requestBody) {
        String payload = serializeResponse(requestBody);
        if (payload == null) {
            payload = "null";
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(payload.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception ex) {
            throw new IllegalStateException("Не удалось вычислить hash запроса", ex);
        }
    }

    private String serializeResponse(Object response) {
        if (response == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Не удалось сериализовать ответ", ex);
        }
    }

    private <T> T deserializeResponse(String json, Class<T> responseType) {
        if (Void.class.equals(responseType)) {
            return null;
        }
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, responseType);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Не удалось десериализовать cached-ответ", ex);
        }
    }
}
