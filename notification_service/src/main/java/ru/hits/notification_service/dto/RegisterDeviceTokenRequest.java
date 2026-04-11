package ru.hits.notification_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterDeviceTokenRequest {

    @NotBlank
    private String token;
}
