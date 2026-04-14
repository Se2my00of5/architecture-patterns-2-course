package ru.hits.notification_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UnregisterDeviceTokenRequest {

    @NotBlank
    private String token;
}
