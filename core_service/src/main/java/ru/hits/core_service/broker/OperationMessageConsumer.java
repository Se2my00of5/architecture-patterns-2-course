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
import ru.hits.core_service.ws.OperationWsPublisher;

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
    private final OperationWsPublisher operationWsPublisher;
    private final OperationCreatedEventProducer operationCreatedEventProducer;

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

        log.info("Received account command: operationId={}, type={}, sourceAccountId={}, targetAccountId={}",
                message.getOperationId(),
                message.getCommandType(),
                message.getSourceAccountId(),
                message.getTargetAccountId());

        try {
            switch (message.getCommandType()) {
                case DEPOSIT -> handleDeposit(message);
                case WITHDRAW -> handleWithdraw(message);
                case TRANSFER -> handleTransfer(message);
                case LOAN_DISBURSEMENT -> handleLoanDisbursement(message);
                case LOAN_REPAYMENT -> handleLoanRepayment(message);
                default -> throw new BusinessException("Неизвестный тип команды: " + message.getCommandType());
            }
            log.info("Processed account command: operationId={}, type={}",
                    message.getOperationId(), message.getCommandType());
        } catch (NotFoundException | BusinessException ex) {
            log.warn("Skip account command operationId={}, type={}, reason={}",
                    message.getOperationId(), message.getCommandType(), ex.getMessage());
        }
    }

    private void handleDeposit(OperationMessage message) {
        AccountEntity account = accountLookupService.findActiveByIdForUpdateOrThrow(message.getSourceAccountId());
        long previousBalance = account.getBalance();
        account.setBalance(accountBalanceService.addAmounts(account.getBalance(), message.getAmount()));
        accountRepository.save(account);

        OperationEntity operation = operationRepository.save(OperationEntity.builder()
                .account(account)
                .type(OperationType.DEPOSIT)
                .amount(message.getAmount())
                .description(message.getDescription())
                .build());
        log.info("Deposit applied: operationId={}, accountId={}, amount={}, balanceBefore={}, balanceAfter={}, recordId={}",
                message.getOperationId(),
                account.getId(),
                message.getAmount(),
                previousBalance,
                account.getBalance(),
                operation.getId());
        operationWsPublisher.publishCreated(operation);
        operationCreatedEventProducer.send(operation);
    }

    private void handleWithdraw(OperationMessage message) {
        AccountEntity account = accountLookupService.findActiveByIdForUpdateOrThrow(message.getSourceAccountId());
        long previousBalance = account.getBalance();
        account.setBalance(accountBalanceService.decreaseBalance(account, message.getAmount()));
        accountRepository.save(account);

        OperationEntity operation = operationRepository.save(OperationEntity.builder()
                .account(account)
                .type(OperationType.WITHDRAWAL)
                .amount(message.getAmount())
                .description(message.getDescription())
                .build());
        log.info("Withdraw applied: operationId={}, accountId={}, amount={}, balanceBefore={}, balanceAfter={}, recordId={}",
                message.getOperationId(),
                account.getId(),
                message.getAmount(),
                previousBalance,
                account.getBalance(),
                operation.getId());
        operationWsPublisher.publishCreated(operation);
        operationCreatedEventProducer.send(operation);
    }

    private void handleTransfer(OperationMessage message) {
        MovementResult movement = executeTwoAccountMovementOrThrow(message);
        AccountEntity sourceAccount = movement.sourceAccount();
        AccountEntity targetAccount = movement.targetAccount();
        long targetAmount = movement.targetAmount();

        String transferOutDescription = transferDescriptionFormatter
                .buildTransferOutDescription(message, targetAccount.getId());
        String transferInDescription = transferDescriptionFormatter
                .buildTransferInDescription(message, sourceAccount.getId());

        OperationEntity outOperation = operationRepository.save(OperationEntity.builder()
                .account(sourceAccount)
                .type(OperationType.TRANSFER_OUT)
                .amount(message.getAmount())
                .description(transferOutDescription)
                .build());

        OperationEntity inOperation = operationRepository.save(OperationEntity.builder()
                .account(targetAccount)
                .type(OperationType.TRANSFER_IN)
                .amount(targetAmount)
                .description(transferInDescription)
                .build());

        log.info("Transfer applied: operationId={}, sourceAccountId={}, targetAccountId={}, sourceAmount={}, targetAmount={}, outRecordId={}, inRecordId={}",
                message.getOperationId(),
                sourceAccount.getId(),
                targetAccount.getId(),
                message.getAmount(),
                targetAmount,
                outOperation.getId(),
                inOperation.getId());

        operationWsPublisher.publishCreated(outOperation);
        operationWsPublisher.publishCreated(inOperation);
        operationCreatedEventProducer.send(outOperation);
        operationCreatedEventProducer.send(inOperation);
    }

    private void handleLoanDisbursement(OperationMessage message) {
        MovementResult movement = executeTwoAccountMovementOrThrow(message);
        AccountEntity targetAccount = movement.targetAccount();
        long targetAmount = movement.targetAmount();

        LoanOperationEntity operation = operationRepository.save(LoanOperationEntity.builder()
                .account(targetAccount)
                .type(OperationType.LOAN_DISBURSEMENT)
                .amount(targetAmount)
                .description(message.getDescription())
                .creditId(message.getCreditId())
                .build());
        log.info("Loan disbursement applied: operationId={}, creditId={}, sourceAccountId={}, targetAccountId={}, sourceAmount={}, targetAmount={}, recordId={}",
                message.getOperationId(),
                message.getCreditId(),
                movement.sourceAccount().getId(),
                targetAccount.getId(),
                message.getAmount(),
                targetAmount,
                operation.getId());
        operationWsPublisher.publishCreated(operation);
        operationCreatedEventProducer.send(operation);
    }

    private void handleLoanRepayment(OperationMessage message) {
        MovementResult movement = executeTwoAccountMovementOrThrow(message);
        AccountEntity sourceAccount = movement.sourceAccount();

        LoanOperationEntity operation = operationRepository.save(LoanOperationEntity.builder()
                .account(sourceAccount)
                .type(OperationType.LOAN_REPAYMENT)
                .amount(message.getAmount())
                .description(message.getDescription())
                .creditId(message.getCreditId())
                .build());
        log.info("Loan repayment applied: operationId={}, creditId={}, sourceAccountId={}, targetAccountId={}, amount={}, recordId={}",
                message.getOperationId(),
                message.getCreditId(),
                sourceAccount.getId(),
                movement.targetAccount().getId(),
                message.getAmount(),
                operation.getId());
        operationWsPublisher.publishCreated(operation);
        operationCreatedEventProducer.send(operation);
    }

    private MovementResult executeTwoAccountMovementOrThrow(OperationMessage message) {
        UUID targetAccountId = message.getTargetAccountId();
        if (targetAccountId == null) {
            throw new BusinessException("Для операции не указан счёт получателя");
        }

        List<UUID> accountIdsToLock = List.of(message.getSourceAccountId(), targetAccountId).stream()
                .sorted(Comparator.naturalOrder())
                .toList();

        AccountEntity firstLocked = accountLookupService.findActiveByIdForUpdateOrThrow(accountIdsToLock.get(0));
        AccountEntity secondLocked = accountLookupService.findActiveByIdForUpdateOrThrow(accountIdsToLock.get(1));

        AccountEntity sourceAccount = firstLocked.getId().equals(message.getSourceAccountId())
                ? firstLocked
                : secondLocked;
        AccountEntity targetAccount = firstLocked.getId().equals(targetAccountId)
                ? firstLocked
                : secondLocked;

        Long targetAmountValue = message.getTargetAmount();
        if (targetAmountValue == null) {
            throw new BusinessException("Для операции не указана сумма зачисления");
        }
        long targetAmount = targetAmountValue;

        sourceAccount.setBalance(accountBalanceService.decreaseBalance(sourceAccount, message.getAmount()));
        targetAccount.setBalance(accountBalanceService.addAmounts(targetAccount.getBalance(), targetAmount));
        accountRepository.save(sourceAccount);
        accountRepository.save(targetAccount);

        log.info("Two-account movement committed: operationId={}, sourceAccountId={}, targetAccountId={}, sourceAmount={}, targetAmount={}",
                message.getOperationId(),
                sourceAccount.getId(),
                targetAccount.getId(),
                message.getAmount(),
                targetAmount);

        return new MovementResult(sourceAccount, targetAccount, targetAmount);
    }

    private record MovementResult(AccountEntity sourceAccount, AccountEntity targetAccount, long targetAmount) {
    }

}
