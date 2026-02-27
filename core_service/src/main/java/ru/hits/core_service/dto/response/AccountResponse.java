package ru.hits.core_service.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.hits.core_service.entity.enums.AccountStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Информация о счёте")
public class AccountResponse {

    @Schema(description = "ID счёта")
    private UUID id;

    @Schema(description = "ID владельца счёта")
    private UUID userId;

    @Schema(description = "Баланс счёта")
    private BigDecimal balance;

    @Schema(description = "Статус счёта")
    private AccountStatus status;

    @Schema(description = "Дата открытия")
    private LocalDateTime createdAt;

    @Schema(description = "Дата закрытия")
    private LocalDateTime closedAt;
}
