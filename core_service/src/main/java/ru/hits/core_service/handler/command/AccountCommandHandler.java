package ru.hits.core_service.handler.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.hits.core_service.broker.OperationMessageProducer;
import ru.hits.core_service.broker.message.AccountCommandType;
import ru.hits.core_service.broker.message.OperationMessage;
import ru.hits.core_service.dto.request.DepositRequest;
import ru.hits.core_service.dto.request.LoanDisbursementRequest;
import ru.hits.core_service.dto.request.LoanRepaymentRequest;
import ru.hits.core_service.dto.request.OpenAccountRequest;
import ru.hits.core_service.dto.request.TransferRequest;
import ru.hits.core_service.dto.request.WithdrawRequest;
import ru.hits.core_service.dto.response.AccountResponse;
import ru.hits.core_service.dto.response.OperationAcceptedResponse;
import ru.hits.core_service.entity.AccountEntity;
import ru.hits.core_service.entity.enums.AccountStatus;
import ru.hits.core_service.exception.BusinessException;
import ru.hits.core_service.exception.NotFoundException;
import ru.hits.core_service.integration.UserServiceClient;
import ru.hits.core_service.mapper.AccountMapper;
import ru.hits.core_service.mapper.MoneyMapper;
import ru.hits.core_service.repository.AccountRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AccountCommandHandler {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final MoneyMapper moneyMapper;
    private final OperationMessageProducer operationMessageProducer;
    private final UserServiceClient userServiceClient;

    /**
     * Открыть новый счёт для клиента.
     */
    public AccountResponse openAccount(OpenAccountRequest command) {
        if (!userServiceClient.userExists(command.getUserId())) {
            throw new NotFoundException("Пользователь не найден: " + command.getUserId());
        }

        AccountEntity account = AccountEntity.builder()
                .userId(command.getUserId())
                .balance(0L)
                .status(AccountStatus.ACTIVE)
                .build();
        return accountMapper.toResponse(accountRepository.save(account));
    }

    /**
     * Закрыть счёт. Проверяет, что баланс нулевой.
     * Проверка активных кредитов — ответственность сервиса кредитов.
     */
    public AccountResponse closeAccount(UUID accountId) {
        AccountEntity account = findActiveAccountForUpdateOrThrow(accountId);

        if (account.getBalance() != 0L) {
            throw new BusinessException("Невозможно закрыть счёт с ненулевым балансом: " + accountId);
        }

        account.setStatus(AccountStatus.CLOSED);
        account.setClosedAt(LocalDateTime.now());
        return accountMapper.toResponse(accountRepository.save(account));
    }

    /**
     * Внести деньги на счёт.
     */
    public OperationAcceptedResponse deposit(UUID accountId, DepositRequest command) {
        log.debug("deposit: accountId={}, amount={}, desc={}", accountId, command.getAmount(), command.getDescription());
        findActiveAccountForUpdateOrThrow(accountId);
        long amountInKopecks = toKopecks(command.getAmount());
        UUID operationId = UUID.randomUUID();

        operationMessageProducer.send(OperationMessage.builder()
                .operationId(operationId)
                .commandType(AccountCommandType.DEPOSIT)
                .sourceAccountId(accountId)
                .amount(amountInKopecks)
                .description(command.getDescription() != null ? command.getDescription() : "Внесение средств на счёт")
                .build());

        return new OperationAcceptedResponse(operationId, "QUEUED");
    }

    /**
     * Снять деньги со счёта.
     */
    public OperationAcceptedResponse withdraw(UUID accountId, WithdrawRequest command) {
        log.debug("withdraw: accountId={}, amount={}, desc={}", accountId, command.getAmount(), command.getDescription());
        long amountInKopecks = toKopecks(command.getAmount());
        AccountEntity account = findActiveAccountForUpdateOrThrow(accountId);
        validateSufficientFunds(account, amountInKopecks);
        UUID operationId = UUID.randomUUID();

        operationMessageProducer.send(OperationMessage.builder()
                .operationId(operationId)
                .commandType(AccountCommandType.WITHDRAW)
                .sourceAccountId(accountId)
                .amount(amountInKopecks)
                .description(command.getDescription() != null ? command.getDescription() : "Снятие средств со счёта")
                .build());

        return new OperationAcceptedResponse(operationId, "QUEUED");
    }

    /**
     * Перевести деньги со счёта на счёт.
     * Поддерживаются переводы как между своими счетами, так и на чужие счета.
     */
    public OperationAcceptedResponse transfer(UUID accountId, TransferRequest command) {
        log.debug("transfer: fromAccountId={}, toAccountId={}, amount={}, desc={}",
                accountId, command.getTargetAccountId(), command.getAmount(), command.getDescription());
        long amountInKopecks = toKopecks(command.getAmount());

        if (accountId.equals(command.getTargetAccountId())) {
            throw new BusinessException("Нельзя выполнить перевод на тот же счёт: " + accountId);
        }
        AccountEntity sourceAccount = findActiveAccountForUpdateOrThrow(accountId);
        validateSufficientFunds(sourceAccount, amountInKopecks);

        UUID operationId = UUID.randomUUID();

        String description = command.getDescription() != null && !command.getDescription().isBlank()
                ? command.getDescription()
                : "Перевод средств";

        operationMessageProducer.send(OperationMessage.builder()
                .operationId(operationId)
                .commandType(AccountCommandType.TRANSFER)
                .sourceAccountId(accountId)
                .targetAccountId(command.getTargetAccountId())
                .amount(amountInKopecks)
                .description(description)
                .build());

        return new OperationAcceptedResponse(operationId, "QUEUED");
    }

    /**
     * Выдать кредит на счёт (пополнение баланса счета).
     */
    public OperationAcceptedResponse loanDisbursement(UUID accountId, LoanDisbursementRequest command) {
        log.debug("loanDisbursement: accountId={}, creditId={}, amount={}", accountId, command.getCreditId(), command.getAmount());
        findActiveAccountForUpdateOrThrow(accountId);
        long amountInKopecks = toKopecks(command.getAmount());
        UUID operationId = UUID.randomUUID();

        operationMessageProducer.send(OperationMessage.builder()
                .operationId(operationId)
                .commandType(AccountCommandType.LOAN_DISBURSEMENT)
                .sourceAccountId(accountId)
                .amount(amountInKopecks)
                .description(command.getDescription() != null ? command.getDescription() : "Выдача кредита")
                .creditId(command.getCreditId())
                .build());

        return new OperationAcceptedResponse(operationId, "QUEUED");
    }

    /**
     * Погасить кредит со счёта (снятие со счета).
     */
    public OperationAcceptedResponse loanRepayment(UUID accountId, LoanRepaymentRequest command) {
        log.debug("loanRepayment: accountId={}, creditId={}, amount={}", accountId, command.getCreditId(), command.getAmount());
        long amountInKopecks = toKopecks(command.getAmount());
        AccountEntity account = findActiveAccountForUpdateOrThrow(accountId);
        validateSufficientFunds(account, amountInKopecks);
        UUID operationId = UUID.randomUUID();

        operationMessageProducer.send(OperationMessage.builder()
                .operationId(operationId)
                .commandType(AccountCommandType.LOAN_REPAYMENT)
                .sourceAccountId(accountId)
                .amount(amountInKopecks)
                .description(command.getDescription() != null ? command.getDescription() : "Погашение кредита")
                .creditId(command.getCreditId())
                .build());

        return new OperationAcceptedResponse(operationId, "QUEUED");
    }


    private AccountEntity findActiveAccountForUpdateOrThrow(UUID accountId) {
        AccountEntity account = accountRepository.findByIdForUpdate(accountId)
                .orElseThrow(() -> new NotFoundException("Счёт не найден: " + accountId));
        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new BusinessException("Счёт закрыт: " + account.getId());
        }
        return account;
    }

    private void validateSufficientFunds(AccountEntity account, long amountInKopecks) {
        if (account.getBalance() < amountInKopecks) {
            throw new BusinessException("Недостаточно средств на счёте: " + account.getId());
        }
    }

    private long toKopecks(BigDecimal amount) {
        try {
            Long amountInKopecks = moneyMapper.rublesToKopecks(amount);
            if (amountInKopecks == null) {
                throw new BusinessException("Сумма обязательна");
            }
            return amountInKopecks;
        } catch (ArithmeticException e) {
            throw new BusinessException("Некорректный формат суммы");
        }
    }
}
