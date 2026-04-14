package ru.hits.core_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.hits.core_service.entity.IdempotencyRecordEntity;
import ru.hits.core_service.repository.IdempotencyRecordRepository;
import ru.hits.shared_resilience.idempotency.IdempotencyExecutor;
import ru.hits.shared_resilience.idempotency.IdempotencyRecordData;
import ru.hits.shared_resilience.idempotency.IdempotencyRecordStore;

import java.util.Optional;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
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
        return new IdempotencyExecutor(buildStore(), objectMapper)
                .execute(scope, idempotencyKey, httpMethod, requestPath, requestBody, responseType, action);
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
        new IdempotencyExecutor(buildStore(), objectMapper)
                .executeVoid(scope, idempotencyKey, httpMethod, requestPath, requestBody, action);
    }

    private IdempotencyRecordStore buildStore() {
        return new IdempotencyRecordStore() {
            @Override
            public Optional<IdempotencyRecordData> findByKey(
                    String idempotencyKey,
                    String scope,
                    String httpMethod,
                    String requestPath
            ) {
                return idempotencyRecordRepository
                        .findByIdempotencyKeyAndScopeAndHttpMethodAndRequestPath(idempotencyKey, scope, httpMethod, requestPath)
                        .map(entity -> IdempotencyRecordData.builder()
                                .idempotencyKey(entity.getIdempotencyKey())
                                .scope(entity.getScope())
                                .httpMethod(entity.getHttpMethod())
                                .requestPath(entity.getRequestPath())
                                .requestHash(entity.getRequestHash())
                                .responseBody(entity.getResponseBody())
                                .createdAt(entity.getCreatedAt())
                                .build());
            }

            @Override
            public void save(IdempotencyRecordData recordData) {
                idempotencyRecordRepository.save(IdempotencyRecordEntity.builder()
                        .idempotencyKey(recordData.getIdempotencyKey())
                        .scope(recordData.getScope())
                        .httpMethod(recordData.getHttpMethod())
                        .requestPath(recordData.getRequestPath())
                        .requestHash(recordData.getRequestHash())
                        .responseBody(recordData.getResponseBody())
                        .createdAt(recordData.getCreatedAt())
                        .build());
            }
        };
    }
}
