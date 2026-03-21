package ru.hits.core_service.ws;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import ru.hits.core_service.entity.OperationEntity;
import ru.hits.core_service.mapper.OperationMapper;

@Component
@RequiredArgsConstructor
public class OperationWsPublisher {

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final OperationMapper operationMapper;

    public void publishCreated(OperationEntity operation) {
        simpMessagingTemplate.convertAndSend(
            OperationWsTopics.accountOperations(operation.getAccount().getId()),
            operationMapper.toResponse(operation)
        );
    }
}