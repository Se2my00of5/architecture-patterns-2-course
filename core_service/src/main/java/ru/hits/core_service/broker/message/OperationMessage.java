package ru.hits.core_service.broker.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.hits.core_service.entity.enums.CurrencyCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperationMessage {

    private UUID operationId;
    private AccountCommandType commandType;
    private UUID sourceAccountId;
    private UUID targetAccountId;
    private Long amount;
    private Long targetAmount;
    private CurrencyCode sourceCurrency;
    private CurrencyCode targetCurrency;
    private BigDecimal exchangeRate;
    private LocalDateTime exchangeRateQuotedAt;
    private String description;
    private UUID creditId;
}
