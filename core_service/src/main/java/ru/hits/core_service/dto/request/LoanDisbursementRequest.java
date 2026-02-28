package ru.hits.core_service.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Schema(description = "Запрос на выполнение выдачи кредита на счёт")
public class LoanDisbursementRequest {

    @NotNull(message = "ID кредита обязателен")
    @Schema(description = "ID кредита")
    private UUID creditId;

    @NotNull(message = "Сумма обязательна")
    @Positive(message = "Сумма должна быть положительной")
    @Schema(description = "Сумма кредита", example = "50000.00")
    private BigDecimal amount;

    @Schema(description = "Описание операции")
    private String description;
}
