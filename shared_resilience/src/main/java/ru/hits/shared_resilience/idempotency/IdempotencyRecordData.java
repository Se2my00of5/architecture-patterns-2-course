package ru.hits.shared_resilience.idempotency;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class IdempotencyRecordData {

    String idempotencyKey;
    String scope;
    String httpMethod;
    String requestPath;
    String requestHash;
    String responseBody;
    LocalDateTime createdAt;
}
