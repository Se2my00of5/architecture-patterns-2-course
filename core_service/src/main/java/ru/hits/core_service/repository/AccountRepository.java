package ru.hits.core_service.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.hits.core_service.entity.AccountEntity;
import ru.hits.core_service.entity.enums.AccountStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<AccountEntity, UUID> {

    List<AccountEntity> findByUserId(UUID userId);

    List<AccountEntity> findByUserIdAndStatus(UUID userId, AccountStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from AccountEntity a where a.id = :accountId")
    Optional<AccountEntity> findByIdForUpdate(@Param("accountId") UUID accountId);
}
