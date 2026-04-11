package ru.hits.notification_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.hits.notification_service.dto.RegisterDeviceTokenRequest;
import ru.hits.notification_service.dto.UnregisterDeviceTokenRequest;
import ru.hits.notification_service.entity.DeviceTokenEntity;
import ru.hits.notification_service.entity.UserRole;
import ru.hits.notification_service.repository.DeviceTokenRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeviceTokenService {

    private final DeviceTokenRepository deviceTokenRepository;
    private final FirebaseTopicService firebaseTopicService;

    @Transactional
    public void registerToken(Jwt jwt, RegisterDeviceTokenRequest request) {
        UUID userId = extractUserId(jwt);
        UserRole role = extractRole(jwt);

        DeviceTokenEntity entity = deviceTokenRepository.findByToken(request.getToken())
                .orElseGet(DeviceTokenEntity::new);

        entity.setToken(request.getToken());
        entity.setUserId(userId);
        entity.setRole(role);
        entity.setActive(true);

        deviceTokenRepository.save(entity);

        firebaseTopicService.subscribeToken(request.getToken(), TopicResolver.forUser(role, userId));
    }

    @Transactional
    public void unregisterToken(Jwt jwt, UnregisterDeviceTokenRequest request) {
        UUID userId = extractUserId(jwt);
        UserRole role = extractRole(jwt);

        deviceTokenRepository.findByToken(request.getToken()).ifPresent(entity -> {
            if (!entity.getUserId().equals(userId)) {
                return;
            }
            entity.setActive(false);
            deviceTokenRepository.save(entity);
            firebaseTopicService.unsubscribeToken(request.getToken(), TopicResolver.forUser(role, userId));
        });
    }

    private UUID extractUserId(Jwt jwt) {
        Object claim = jwt.getClaims().get("user_id");
        if (claim == null) {
            throw new IllegalArgumentException("user_id claim is required");
        }
        return UUID.fromString(String.valueOf(claim));
    }

    private UserRole extractRole(Jwt jwt) {
        String roles = String.valueOf(jwt.getClaims().getOrDefault("roles", "CLIENT"));
        if (roles.contains("EMPLOYEE")) {
            return UserRole.EMPLOYEE;
        }
        return UserRole.CLIENT;
    }
}
