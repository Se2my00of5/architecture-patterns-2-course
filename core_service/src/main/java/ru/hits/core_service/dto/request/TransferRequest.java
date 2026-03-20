package ru.hits.core_service.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Schema(description = "Запрос на перевод средств между счетами")
public class TransferRequest {

    @NotNull(message = "Счёт получателя обязателен")
    @Schema(description = "ID счёта получателя", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
    private UUID targetAccountId;

    @NotNull(message = "Сумма обязательна")
    @Positive(message = "Сумма должна быть положительной")
    @Digits(integer = 17, fraction = 2, message = "Сумма должна содержать не более 2 знаков после запятой")
    @Schema(description = "Сумма перевода", example = "1000.00")
    private BigDecimal amount;

    @Schema(description = "Описание операции")
    private String description;
}