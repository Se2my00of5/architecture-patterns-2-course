package ru.hits.core_service.broker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.hits.core_service.broker.formatter.TransferDescriptionFormatter;
import ru.hits.core_service.broker.message.OperationMessage;
import ru.hits.core_service.entity.AccountEntity;
import ru.hits.core_service.entity.LoanOperationEntity;
import ru.hits.core_service.entity.OperationEntity;
import ru.hits.core_service.entity.enums.OperationType;
import ru.hits.core_service.exception.BusinessException;
import ru.hits.core_service.exception.NotFoundException;
import ru.hits.core_service.repository.AccountRepository;
import ru.hits.core_service.repository.OperationRepository;
import ru.hits.core_service.service.AccountBalanceService;
import ru.hits.core_service.service.AccountLookupService;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class OperationMessageConsumer {

    private final OperationRepository operationRepository;
    private final AccountRepository accountRepository;
    private final ObjectMapper objectMapper;
    private final TransferDescriptionFormatter transferDescriptionFormatter;
    private final AccountLookupService accountLookupService;
    private final AccountBalanceService accountBalanceService;

    @KafkaListener(topics = "${app.kafka.topics.account-operations}")
    @Transactional
    public void consume(String payload) {
        OperationMessage message;
        try {
            message = objectMapper.readValue(payload, OperationMessage.class);
        } catch (JsonProcessingException e) {
            log.warn("Skip account command due to invalid JSON payload: {}", payload);
            return;
        }

        try {
            switch (message.getCommandType()) {
                case DEPOSIT -> handleDeposit(message);
                case WITHDRAW -> handleWithdraw(message);
                case TRANSFER -> handleTransfer(message);
                case LOAN_DISBURSEMENT -> handleLoanDisbursement(message);
                case LOAN_REPAYMENT -> handleLoanRepayment(message);
                default -> throw new BusinessException("Неизвестный тип команды: " + message.getCommandType());
            }
        } catch (NotFoundException | BusinessException ex) {
            log.warn("Skip account command operationId={}, type={}, reason={}",
                    message.getOperationId(), message.getCommandType(), ex.getMessage());
        }
    }

    private void handleDeposit(OperationMessage message) {
        AccountEntity account = accountLookupService.findActiveByIdForUpdateOrThrow(message.getSourceAccountId());
        account.setBalance(accountBalanceService.addAmounts(account.getBalance(), message.getAmount()));
        accountRepository.save(account);

        operationRepository.save(OperationEntity.builder()
                .account(account)
                .type(OperationType.DEPOSIT)
                .amount(message.getAmount())
                .description(message.getDescription())
                .build());
    }

    private void handleWithdraw(OperationMessage message) {
        AccountEntity account = accountLookupService.findActiveByIdForUpdateOrThrow(message.getSourceAccountId());
        account.setBalance(accountBalanceService.decreaseBalance(account, message.getAmount()));
        accountRepository.save(account);

        operationRepository.save(OperationEntity.builder()
                .account(account)
                .type(OperationType.WITHDRAWAL)
                .amount(message.getAmount())
                .description(message.getDescription())
                .build());
    }

    private void handleTransfer(OperationMessage message) {
        if (message.getTargetAccountId() == null) {
            throw new BusinessException("Для перевода не указан счёт получателя");
        }

        List<UUID> accountIdsToLock = List.of(message.getSourceAccountId(), message.getTargetAccountId()).stream()
                .sorted(Comparator.naturalOrder())
                .toList();

        AccountEntity firstLocked = accountLookupService.findActiveByIdForUpdateOrThrow(accountIdsToLock.get(0));
        AccountEntity secondLocked = accountLookupService.findActiveByIdForUpdateOrThrow(accountIdsToLock.get(1));

        AccountEntity sourceAccount = firstLocked.getId().equals(message.getSourceAccountId()) ? firstLocked : secondLocked;
        AccountEntity targetAccount = firstLocked.getId().equals(message.getTargetAccountId()) ? firstLocked : secondLocked;

        long targetAmount = message.getTargetAmount() != null ? message.getTargetAmount() : message.getAmount();

        sourceAccount.setBalance(accountBalanceService.decreaseBalance(sourceAccount, message.getAmount()));
        targetAccount.setBalance(accountBalanceService.addAmounts(targetAccount.getBalance(), targetAmount));
        accountRepository.save(sourceAccount);
        accountRepository.save(targetAccount);

        String transferOutDescription = transferDescriptionFormatter
                .buildTransferOutDescription(message, targetAccount.getId());
        String transferInDescription = transferDescriptionFormatter
                .buildTransferInDescription(message, sourceAccount.getId());

        operationRepository.save(OperationEntity.builder()
                .account(sourceAccount)
                .type(OperationType.TRANSFER_OUT)
                .amount(message.getAmount())
                .description(transferOutDescription)
                .build());

        operationRepository.save(OperationEntity.builder()
                .account(targetAccount)
                .type(OperationType.TRANSFER_IN)
                .amount(targetAmount)
                .description(transferInDescription)
                .build());
    }

    private void handleLoanDisbursement(OperationMessage message) {
        AccountEntity account = accountLookupService.findActiveByIdForUpdateOrThrow(message.getSourceAccountId());
        account.setBalance(accountBalanceService.addAmounts(account.getBalance(), message.getAmount()));
        accountRepository.save(account);

        operationRepository.save(LoanOperationEntity.builder()
                .account(account)
                .type(OperationType.LOAN_DISBURSEMENT)
                .amount(message.getAmount())
                .description(message.getDescription())
                .creditId(message.getCreditId())
                .build());
    }

    private void handleLoanRepayment(OperationMessage message) {
        AccountEntity account = accountLookupService.findActiveByIdForUpdateOrThrow(message.getSourceAccountId());
        account.setBalance(accountBalanceService.decreaseBalance(account, message.getAmount()));
        accountRepository.save(account);

        operationRepository.save(LoanOperationEntity.builder()
                .account(account)
                .type(OperationType.LOAN_REPAYMENT)
                .amount(message.getAmount())
                .description(message.getDescription())
                .creditId(message.getCreditId())
                .build());
    }

}
