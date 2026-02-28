package ru.hits.core_service.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.hits.core_service.entity.enums.OperationType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Информация об операции")
public class OperationResponse {

    @Schema(description = "ID операции")
    private UUID id;

    @Schema(description = "ID счёта")
    private UUID accountId;

    @Schema(description = "ID кредита (необязательно)")
    private UUID creditId;

    @Schema(description = "Тип операции")
    private OperationType type;

    @Schema(description = "Сумма операции")
    private BigDecimal amount;

    @Schema(description = "Дата операции")
    private LocalDateTime createdAt;

    @Schema(description = "Описание операции")
    private String description;
}
