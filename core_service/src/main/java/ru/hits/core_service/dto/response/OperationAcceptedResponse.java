package ru.hits.core_service.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Ответ о постановке операции в очередь")
public class OperationAcceptedResponse {

    @Schema(description = "ID операции в очереди")
    private UUID operationId;

    @Schema(description = "Статус постановки в очередь", example = "QUEUED")
    private String status;
}
