package ru.hits.core_service.broker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.hits.core_service.broker.message.OperationMessage;
import ru.hits.core_service.entity.AccountEntity;
import ru.hits.core_service.entity.LoanOperationEntity;
import ru.hits.core_service.entity.OperationEntity;
import ru.hits.core_service.entity.enums.AccountStatus;
import ru.hits.core_service.entity.enums.OperationType;
import ru.hits.core_service.exception.BusinessException;
import ru.hits.core_service.exception.NotFoundException;
import ru.hits.core_service.repository.AccountRepository;
import ru.hits.core_service.repository.OperationRepository;

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
        AccountEntity account = findActiveAccountForUpdateOrThrow(message.getSourceAccountId());
        account.setBalance(addAmounts(account.getBalance(), message.getAmount()));
        accountRepository.save(account);

        operationRepository.save(OperationEntity.builder()
                .account(account)
                .type(OperationType.DEPOSIT)
                .amount(message.getAmount())
                .description(message.getDescription())
                .build());
    }

    private void handleWithdraw(OperationMessage message) {
        AccountEntity account = findActiveAccountForUpdateOrThrow(message.getSourceAccountId());
        account.setBalance(decreaseBalance(account, message.getAmount()));
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

        AccountEntity firstLocked = findActiveAccountForUpdateOrThrow(accountIdsToLock.get(0));
        AccountEntity secondLocked = findActiveAccountForUpdateOrThrow(accountIdsToLock.get(1));

        AccountEntity sourceAccount = firstLocked.getId().equals(message.getSourceAccountId()) ? firstLocked : secondLocked;
        AccountEntity targetAccount = firstLocked.getId().equals(message.getTargetAccountId()) ? firstLocked : secondLocked;

        sourceAccount.setBalance(decreaseBalance(sourceAccount, message.getAmount()));
        targetAccount.setBalance(addAmounts(targetAccount.getBalance(), message.getAmount()));
        accountRepository.save(sourceAccount);
        accountRepository.save(targetAccount);

        operationRepository.save(OperationEntity.builder()
                .account(sourceAccount)
                .type(OperationType.TRANSFER_OUT)
                .amount(message.getAmount())
                .description(message.getDescription() + ". Получатель: " + targetAccount.getId())
                .build());

        operationRepository.save(OperationEntity.builder()
                .account(targetAccount)
                .type(OperationType.TRANSFER_IN)
                .amount(message.getAmount())
                .description(message.getDescription() + ". Отправитель: " + sourceAccount.getId())
                .build());
    }

    private void handleLoanDisbursement(OperationMessage message) {
        AccountEntity account = findActiveAccountForUpdateOrThrow(message.getSourceAccountId());
        account.setBalance(addAmounts(account.getBalance(), message.getAmount()));
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
        AccountEntity account = findActiveAccountForUpdateOrThrow(message.getSourceAccountId());
        account.setBalance(decreaseBalance(account, message.getAmount()));
        accountRepository.save(account);

        operationRepository.save(LoanOperationEntity.builder()
                .account(account)
                .type(OperationType.LOAN_REPAYMENT)
                .amount(message.getAmount())
                .description(message.getDescription())
                .creditId(message.getCreditId())
                .build());
    }

    private AccountEntity findActiveAccountForUpdateOrThrow(UUID accountId) {
        AccountEntity account = accountRepository.findByIdForUpdate(accountId)
                .orElseThrow(() -> new NotFoundException("Счёт не найден: " + accountId));

        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new BusinessException("Счёт закрыт: " + account.getId());
        }

        return account;
    }

    private long decreaseBalance(AccountEntity account, long amountInKopecks) {
        if (account.getBalance() < amountInKopecks) {
            throw new BusinessException("Недостаточно средств на счёте: " + account.getId());
        }
        long result = subtractAmounts(account.getBalance(), amountInKopecks);
        if (result < 0) {
            throw new BusinessException("Операция привела бы к отрицательному балансу: " + account.getId());
        }
        return result;
    }

    private long addAmounts(long left, long right) {
        try {
            return Math.addExact(left, right);
        } catch (ArithmeticException e) {
            throw new BusinessException("Переполнение при расчёте суммы");
        }
    }

    private long subtractAmounts(long left, long right) {
        try {
            return Math.subtractExact(left, right);
        } catch (ArithmeticException e) {
            throw new BusinessException("Переполнение при расчёте суммы");
        }
    }
}
