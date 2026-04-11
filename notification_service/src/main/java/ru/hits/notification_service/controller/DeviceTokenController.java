package ru.hits.notification_service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.hits.notification_service.dto.RegisterDeviceTokenRequest;
import ru.hits.notification_service.dto.UnregisterDeviceTokenRequest;
import ru.hits.notification_service.service.DeviceTokenService;

@RestController
@RequestMapping("/api/push/tokens")
@RequiredArgsConstructor
public class DeviceTokenController {

    private final DeviceTokenService deviceTokenService;

    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void register(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody @Valid RegisterDeviceTokenRequest request
    ) {
        deviceTokenService.registerToken(jwt, request);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unregister(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody @Valid UnregisterDeviceTokenRequest request
    ) {
        deviceTokenService.unregisterToken(jwt, request);
    }
}
