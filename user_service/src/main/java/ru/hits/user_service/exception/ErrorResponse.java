package ru.hits.user_service.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
@Schema(description = "Ответ с ошибкой")
public class ErrorResponse {

    @Schema(description = "Сообщение об ошибке")
    private String message;

    @Schema(description = "Время ошибки")
    private Instant timestamp;
}
