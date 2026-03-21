package ru.hits.core_service.broker.formatter;

import org.springframework.stereotype.Component;
import ru.hits.core_service.broker.message.OperationMessage;

import java.util.UUID;

@Component
public class TransferDescriptionFormatter {

    public String buildTransferOutDescription(OperationMessage message, UUID targetAccountId) {
        if (isFxTransfer(message)) {
            return String.format("%s. Получатель: %s. Конвертация %s->%s, курс %s",
                    message.getDescription(),
                    targetAccountId,
                    message.getSourceCurrency(),
                    message.getTargetCurrency(),
                    message.getExchangeRate());
        }
        return message.getDescription() + ". Получатель: " + targetAccountId;
    }

    public String buildTransferInDescription(OperationMessage message, UUID sourceAccountId) {
        if (isFxTransfer(message)) {
            return String.format("%s. Отправитель: %s. Конвертация %s->%s, курс %s",
                    message.getDescription(),
                    sourceAccountId,
                    message.getSourceCurrency(),
                    message.getTargetCurrency(),
                    message.getExchangeRate());
        }
        return message.getDescription() + ". Отправитель: " + sourceAccountId;
    }

    private boolean isFxTransfer(OperationMessage message) {
        return message.getSourceCurrency() != null
                && message.getTargetCurrency() != null
                && message.getSourceCurrency() != message.getTargetCurrency()
                && message.getExchangeRate() != null
                && message.getTargetAmount() != null;
    }
}