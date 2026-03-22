package ru.hits.user_service.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Запрос на logout")
public class LogoutRequest {

    @Schema(description = "Refresh token для отзыва (необязательно)")
    private String refreshToken;
}
