package ru.hits.notification_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.hits.notification_service.entity.DeviceTokenEntity;

import java.util.Optional;
import java.util.UUID;

public interface DeviceTokenRepository extends JpaRepository<DeviceTokenEntity, UUID> {

    Optional<DeviceTokenEntity> findByToken(String token);
}
