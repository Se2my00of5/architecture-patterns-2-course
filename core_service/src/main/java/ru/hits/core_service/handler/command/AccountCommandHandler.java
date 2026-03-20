package ru.hits.core_service.handler.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.hits.core_service.dto.request.DepositRequest;
import ru.hits.core_service.dto.request.LoanDisbursementRequest;
import ru.hits.core_service.dto.request.LoanRepaymentRequest;
import ru.hits.core_service.dto.request.OpenAccountRequest;
import ru.hits.core_service.dto.request.TransferRequest;
import ru.hits.core_service.dto.request.WithdrawRequest;
import ru.hits.core_service.dto.response.AccountResponse;
import ru.hits.core_service.entity.AccountEntity;
import ru.hits.core_service.entity.LoanOperationEntity;
import ru.hits.core_service.entity.OperationEntity;
import ru.hits.core_service.entity.enums.AccountStatus;
import ru.hits.core_service.entity.enums.OperationType;
import ru.hits.core_service.exception.BusinessException;
import ru.hits.core_service.exception.NotFoundException;
import ru.hits.core_service.integration.UserServiceClient;
import ru.hits.core_service.mapper.AccountMapper;
import ru.hits.core_service.mapper.MoneyMapper;
import ru.hits.core_service.repository.AccountRepository;
import ru.hits.core_service.repository.OperationRepository;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AccountCommandHandler {

    private final AccountRepository accountRepository;
    private final OperationRepository operationRepository;
    private final AccountMapper accountMapper;
    private final MoneyMapper moneyMapper;
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
    public AccountResponse deposit(UUID accountId, DepositRequest command) {
        log.debug("deposit: accountId={}, amount={}, desc={}", accountId, command.getAmount(), command.getDescription());
        AccountEntity account = findActiveAccountForUpdateOrThrow(accountId);
        long amountInKopecks = toKopecks(command.getAmount());

        account.setBalance(addAmounts(account.getBalance(), amountInKopecks));
        accountRepository.save(account);

        OperationEntity operation = OperationEntity.builder()
                .account(account)
                .type(OperationType.DEPOSIT)
            .amount(amountInKopecks)
                .description(command.getDescription() != null ? command.getDescription() : "Внесение средств на счёт")
                .build();
        operationRepository.save(operation);

        return accountMapper.toResponse(account);
    }

    /**
     * Снять деньги со счёта.
     */
    public AccountResponse withdraw(UUID accountId, WithdrawRequest command) {
        log.debug("withdraw: accountId={}, amount={}, desc={}", accountId, command.getAmount(), command.getDescription());
        AccountEntity account = findActiveAccountForUpdateOrThrow(accountId);
        long amountInKopecks = toKopecks(command.getAmount());

        account.setBalance(decreaseBalance(account, amountInKopecks));
        accountRepository.save(account);

        OperationEntity operation = OperationEntity.builder()
                .account(account)
                .type(OperationType.WITHDRAWAL)
            .amount(amountInKopecks)
                .description(command.getDescription() != null ? command.getDescription() : "Снятие средств со счёта")
                .build();
        operationRepository.save(operation);

        return accountMapper.toResponse(account);
    }

    /**
     * Перевести деньги со счёта на счёт.
     * Поддерживаются переводы как между своими счетами, так и на чужие счета.
     */
    public AccountResponse transfer(UUID accountId, TransferRequest command) {
        log.debug("transfer: fromAccountId={}, toAccountId={}, amount={}, desc={}",
                accountId, command.getTargetAccountId(), command.getAmount(), command.getDescription());
        long amountInKopecks = toKopecks(command.getAmount());

        if (accountId.equals(command.getTargetAccountId())) {
            throw new BusinessException("Нельзя выполнить перевод на тот же счёт: " + accountId);
        }


        // Cортировка ID для предотвращения дедлоков при одновременных переводах между одними и теми же счетами в разных направлениях
        // Если поток A лочит сначала A, потом B, а поток B — сначала B, потом A, то может возникнуть дедлок.
        List<UUID> accountIdsToLock = List.of(accountId, command.getTargetAccountId()).stream()
                .sorted(Comparator.naturalOrder())
                .toList();

        AccountEntity firstLocked = findActiveAccountForUpdateOrThrow(accountIdsToLock.get(0));
        AccountEntity secondLocked = findActiveAccountForUpdateOrThrow(accountIdsToLock.get(1));

        AccountEntity sourceAccount = firstLocked.getId().equals(accountId) ? firstLocked : secondLocked;
        AccountEntity targetAccount = firstLocked.getId().equals(command.getTargetAccountId()) ? firstLocked : secondLocked;

        sourceAccount.setBalance(decreaseBalance(sourceAccount, amountInKopecks));
        targetAccount.setBalance(addAmounts(targetAccount.getBalance(), amountInKopecks));
        accountRepository.save(sourceAccount);
        accountRepository.save(targetAccount);

        String description = command.getDescription() != null && !command.getDescription().isBlank()
                ? command.getDescription()
                : "Перевод средств";

        OperationEntity transferOutOperation = OperationEntity.builder()
                .account(sourceAccount)
                .type(OperationType.TRANSFER_OUT)
            .amount(amountInKopecks)
                .description(description + ". Получатель: " + targetAccount.getId())
                .build();
        operationRepository.save(transferOutOperation);

        OperationEntity transferInOperation = OperationEntity.builder()
                .account(targetAccount)
                .type(OperationType.TRANSFER_IN)
            .amount(amountInKopecks)
                .description(description + ". Отправитель: " + sourceAccount.getId())
                .build();
        operationRepository.save(transferInOperation);

        return accountMapper.toResponse(sourceAccount);
    }

    /**
     * Выдать кредит на счёт (пополнение баланса счета).
     */
    public AccountResponse loanDisbursement(UUID accountId, LoanDisbursementRequest command) {
        log.debug("loanDisbursement: accountId={}, creditId={}, amount={}", accountId, command.getCreditId(), command.getAmount());
        AccountEntity account = findActiveAccountForUpdateOrThrow(accountId);
        long amountInKopecks = toKopecks(command.getAmount());

        account.setBalance(addAmounts(account.getBalance(), amountInKopecks));
        accountRepository.save(account);

        LoanOperationEntity operation = LoanOperationEntity.builder()
                .account(account)
                .type(OperationType.LOAN_DISBURSEMENT)
            .amount(amountInKopecks)
                .description(command.getDescription() != null ? command.getDescription() : "Выдача кредита")
                .creditId(command.getCreditId())
                .build();
        operationRepository.save(operation);

        return accountMapper.toResponse(account);
    }

    /**
     * Погасить кредит со счёта (снятие со счета).
     */
    public AccountResponse loanRepayment(UUID accountId, LoanRepaymentRequest command) {
        log.debug("loanRepayment: accountId={}, creditId={}, amount={}", accountId, command.getCreditId(), command.getAmount());
        AccountEntity account = findActiveAccountForUpdateOrThrow(accountId);
        long amountInKopecks = toKopecks(command.getAmount());

        account.setBalance(decreaseBalance(account, amountInKopecks));
        accountRepository.save(account);

        LoanOperationEntity operation = LoanOperationEntity.builder()
                .account(account)
                .type(OperationType.LOAN_REPAYMENT)
            .amount(amountInKopecks)
                .description(command.getDescription() != null ? command.getDescription() : "Погашение кредита")
                .creditId(command.getCreditId())
                .build();
        operationRepository.save(operation);

        return accountMapper.toResponse(account);
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
