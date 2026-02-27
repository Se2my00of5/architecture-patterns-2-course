package ru.hits.core_service.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
@Schema(description = "Запрос на открытие нового счёта")
public class OpenAccountRequest {

    @NotNull(message = "ID пользователя обязателен")
    @Schema(description = "ID клиента, для которого открывается счёт")
    private UUID userId;
}
