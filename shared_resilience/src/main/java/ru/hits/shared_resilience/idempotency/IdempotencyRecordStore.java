package ru.hits.shared_resilience.idempotency;

import java.util.Optional;

public interface IdempotencyRecordStore {

    Optional<IdempotencyRecordData> findByKey(String idempotencyKey, String scope, String httpMethod, String requestPath);

    void save(IdempotencyRecordData recordData);
}
