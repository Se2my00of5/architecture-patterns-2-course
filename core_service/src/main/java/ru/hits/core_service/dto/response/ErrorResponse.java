package ru.hits.core_service.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Информация об ошибке")
public class ErrorResponse {

    @Schema(description = "Сообщение об ошибке")
    private String message;

    @Schema(description = "Время ошибки")
    private LocalDateTime timestamp;
}
