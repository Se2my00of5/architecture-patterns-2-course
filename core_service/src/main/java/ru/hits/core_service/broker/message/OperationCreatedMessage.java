package ru.hits.core_service.broker.message;

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
public class OperationCreatedMessage {

    private UUID id;
    private UUID accountId;
    private UUID creditId;
    private UUID userId;
    private OperationType type;
    private BigDecimal amount;
    private LocalDateTime createdAt;
    private String description;
}
