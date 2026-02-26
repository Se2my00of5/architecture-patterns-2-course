package ru.hits.core_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.hits.core_service.entity.OperationEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface OperationRepository extends JpaRepository<OperationEntity, UUID> {

    List<OperationEntity> findByAccountIdOrderByCreatedAtDesc(UUID accountId);
}
