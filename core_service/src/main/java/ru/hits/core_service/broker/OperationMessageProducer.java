package ru.hits.core_service.broker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.hits.core_service.broker.message.OperationMessage;
import ru.hits.core_service.exception.BusinessException;

import java.util.concurrent.ExecutionException;

@Component
@RequiredArgsConstructor
@Slf4j
public class OperationMessageProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.kafka.topics.account-operations}")
    private String accountOperationsTopic;

    public void send(OperationMessage message) {
        try {
            String payload = objectMapper.writeValueAsString(message);
            kafkaTemplate.send(accountOperationsTopic, message.getSourceAccountId().toString(), payload).get();
        } catch (JsonProcessingException e) {
            throw new BusinessException("Не удалось сериализовать сообщение операции");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException("Операция прервана при отправке в брокер сообщений");
        } catch (ExecutionException e) {
            log.error("Failed to send operation message to Kafka", e);
            throw new BusinessException("Не удалось поставить операцию в очередь");
        }
    }
}
