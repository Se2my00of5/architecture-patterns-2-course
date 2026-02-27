package ru.hits.core_service.handler.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import ru.hits.core_service.dto.request.DepositRequest;
import ru.hits.core_service.dto.request.OpenAccountRequest;
import ru.hits.core_service.dto.request.WithdrawRequest;
import ru.hits.core_service.dto.response.AccountResponse;
import ru.hits.core_service.entity.AccountEntity;
import ru.hits.core_service.entity.OperationEntity;
import ru.hits.core_service.entity.enums.AccountStatus;
import ru.hits.core_service.entity.enums.OperationType;
import ru.hits.core_service.exception.BusinessException;
import ru.hits.core_service.exception.NotFoundException;
import ru.hits.core_service.mapper.AccountMapper;
import ru.hits.core_service.repository.AccountRepository;
import ru.hits.core_service.repository.OperationRepository;

import java.math.BigDecimal;
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

    /**
     * Открыть новый счёт для клиента.
     */
    public AccountResponse openAccount(OpenAccountRequest command) {
        AccountEntity account = AccountEntity.builder()
                .userId(command.getUserId())
                .balance(BigDecimal.ZERO)
                .status(AccountStatus.ACTIVE)
                .build();
        return accountMapper.toResponse(accountRepository.save(account));
    }

    /**
     * Закрыть счёт. Проверяет, что баланс нулевой.
     * Проверка активных кредитов — ответственность сервиса кредитов.
     */
    public AccountResponse closeAccount(UUID accountId) {
        AccountEntity account = findAccountOrThrow(accountId);

        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new BusinessException("Счёт уже закрыт: " + accountId);
        }

        if (account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
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
        AccountEntity account = findActiveAccountOrThrow(accountId);

        account.setBalance(account.getBalance().add(command.getAmount()));
        accountRepository.save(account);

        OperationEntity operation = OperationEntity.builder()
                .account(account)
                .type(OperationType.DEPOSIT)
                .amount(command.getAmount())
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
        AccountEntity account = findActiveAccountOrThrow(accountId);

        if (account.getBalance().compareTo(command.getAmount()) < 0) {
            throw new BusinessException("Недостаточно средств на счёте: " + accountId);
        }

        account.setBalance(account.getBalance().subtract(command.getAmount()));
        accountRepository.save(account);

        OperationEntity operation = OperationEntity.builder()
                .account(account)
                .type(OperationType.WITHDRAWAL)
                .amount(command.getAmount())
                .description(command.getDescription() != null ? command.getDescription() : "Снятие средств со счёта")
                .build();
        operationRepository.save(operation);

        return accountMapper.toResponse(account);
    }

    private AccountEntity findAccountOrThrow(UUID accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Счёт не найден: " + accountId));
    }

    private AccountEntity findActiveAccountOrThrow(UUID accountId) {
        AccountEntity account = findAccountOrThrow(accountId);
        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new BusinessException("Счёт закрыт: " + accountId);
        }
        return account;
    }
}
