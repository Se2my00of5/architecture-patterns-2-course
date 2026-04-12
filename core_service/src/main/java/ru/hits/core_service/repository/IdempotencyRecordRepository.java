package ru.hits.core_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.hits.core_service.entity.IdempotencyRecordEntity;

import java.util.Optional;
import java.util.UUID;

public interface IdempotencyRecordRepository extends JpaRepository<IdempotencyRecordEntity, UUID> {

    Optional<IdempotencyRecordEntity> findByIdempotencyKeyAndScopeAndHttpMethodAndRequestPath(
            String idempotencyKey,
            String scope,
            String httpMethod,
            String requestPath
    );
}
