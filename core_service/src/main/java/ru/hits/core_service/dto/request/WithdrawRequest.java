package ru.hits.core_service.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Запрос на снятие средств со счёта")
public class WithdrawRequest {

    @NotNull(message = "Сумма обязательна")
    @Positive(message = "Сумма должна быть положительной")
    @Schema(description = "Сумма для снятия", example = "1000.00")
    private BigDecimal amount;

    @Schema(description = "Описание операции")
    private String description;
}
