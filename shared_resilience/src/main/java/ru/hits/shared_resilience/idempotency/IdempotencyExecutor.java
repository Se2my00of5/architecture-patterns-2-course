package ru.hits.shared_resilience.idempotency;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import ru.hits.shared_resilience.exception.IdempotencyConflictException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.function.Supplier;

@RequiredArgsConstructor
@Slf4j
public class IdempotencyExecutor {

    private final IdempotencyRecordStore store;
    private final ObjectMapper objectMapper;

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

    public <T> T execute(
            String scope,
            String idempotencyKey,
            String httpMethod,
            String requestPath,
            Object requestBody,
            Class<T> responseType,
            Supplier<T> action
    ) {
        log.info("Executing Idempotency Executor, shared_rel");
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            log.error("idempotencyKey is null or empty");
            throw new IdempotencyConflictException("Отсутствует заголовок Idempotency-Key");
        }

        String requestHash = buildRequestHash(requestBody);

        var existing = store.findByKey(idempotencyKey, scope, httpMethod, requestPath);
        if (existing.isPresent()) {
            IdempotencyRecordData record = existing.get();
            if (!record.getRequestHash().equals(requestHash)) {
                throw new IdempotencyConflictException("Idempotency-Key уже использован для другого содержимого запроса");
            }
            return deserializeResponse(record.getResponseBody(), responseType);
        }

        T response = action.get();

        IdempotencyRecordData data = IdempotencyRecordData.builder()
                .idempotencyKey(idempotencyKey)
                .scope(scope)
                .httpMethod(httpMethod)
                .requestPath(requestPath)
                .requestHash(requestHash)
                .responseBody(serialize(response))
                .createdAt(LocalDateTime.now())
                .build();

        try {
            store.save(data);
        } catch (DataIntegrityViolationException ex) {
            log.debug("Idempotency race detected for key={}, scope={}", idempotencyKey, scope);
            var afterRace = store.findByKey(idempotencyKey, scope, httpMethod, requestPath).orElseThrow(() -> ex);
            if (!afterRace.getRequestHash().equals(requestHash)) {
                throw new IdempotencyConflictException("Idempotency-Key уже использован для другого содержимого запроса");
            }
            return deserializeResponse(afterRace.getResponseBody(), responseType);
        }

        return response;
    }

    private String buildRequestHash(Object requestBody) {
        String payload = serialize(requestBody);
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

    private String serialize(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Не удалось сериализовать значение", ex);
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
