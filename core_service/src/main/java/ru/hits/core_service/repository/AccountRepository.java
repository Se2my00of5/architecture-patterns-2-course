package ru.hits.core_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.hits.core_service.entity.AccountEntity;
import ru.hits.core_service.entity.enums.AccountStatus;

import java.util.List;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<AccountEntity, UUID> {

    List<AccountEntity> findByUserId(UUID userId);

    List<AccountEntity> findByUserIdAndStatus(UUID userId, AccountStatus status);
}
