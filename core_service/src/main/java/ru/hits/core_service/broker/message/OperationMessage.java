package ru.hits.core_service.broker.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private String description;
    private UUID creditId;
}
