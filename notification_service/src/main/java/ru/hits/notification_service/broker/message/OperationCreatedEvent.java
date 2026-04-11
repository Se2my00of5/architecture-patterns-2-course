package ru.hits.notification_service.broker.message;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OperationCreatedEvent {

    private UUID id;
    private UUID accountId;
    private UUID creditId;
    private UUID userId;
    private String type;
    private BigDecimal amount;
    private LocalDateTime createdAt;
    private String description;
}