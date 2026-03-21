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
import ru.hits.core_service.integration.CurrencyConversionService;
import ru.hits.core_service.integration.UserServiceClient;
import ru.hits.core_service.mapper.AccountMapper;
import ru.hits.core_service.mapper.MoneyMapper;
import ru.hits.core_service.repository.AccountRepository;
import ru.hits.core_service.service.AccountBalanceService;
import ru.hits.core_service.service.AccountLookupService;
import ru.hits.core_service.service.BankMasterAccountService;

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
    private final CurrencyConversionService currencyConversionService;
    private final AccountLookupService accountLookupService;
    private final AccountBalanceService accountBalanceService;
    private final BankMasterAccountService bankMasterAccountService;

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
                .currency(command.getCurrency())
                .status(AccountStatus.ACTIVE)
                .build();
        return accountMapper.toResponse(accountRepository.save(account));
    }

    /**
     * Закрыть счёт. Проверяет, что баланс нулевой.
     * Проверка активных кредитов — ответственность сервиса кредитов.
     */
    public AccountResponse closeAccount(UUID accountId) {
        AccountEntity account = accountLookupService.findActiveByIdForUpdateOrThrow(accountId);

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
        log.debug("deposit: accountId={}, amount={}, desc={}", accountId, command.getAmount(),
                command.getDescription());
        accountLookupService.findActiveByIdForUpdateOrThrow(accountId);
        long amountInMinorUnits = moneyMapper.majorToMinor(command.getAmount());
        UUID operationId = UUID.randomUUID();

        operationMessageProducer.send(OperationMessage.builder()
                .operationId(operationId)
                .commandType(AccountCommandType.DEPOSIT)
                .sourceAccountId(accountId)
                .amount(amountInMinorUnits)
                .description(command.getDescription() != null ? command.getDescription() : "Внесение средств на счёт")
                .build());

        return new OperationAcceptedResponse(operationId, "QUEUED");
    }

    /**
     * Снять деньги со счёта.
     */
    public OperationAcceptedResponse withdraw(UUID accountId, WithdrawRequest command) {
        log.debug("withdraw: accountId={}, amount={}, desc={}", accountId, command.getAmount(),
                command.getDescription());
        long amountInMinorUnits = moneyMapper.majorToMinor(command.getAmount());
        AccountEntity account = accountLookupService.findActiveByIdForUpdateOrThrow(accountId);
        accountBalanceService.ensureSufficientFunds(account, amountInMinorUnits);
        UUID operationId = UUID.randomUUID();

        operationMessageProducer.send(OperationMessage.builder()
                .operationId(operationId)
                .commandType(AccountCommandType.WITHDRAW)
                .sourceAccountId(accountId)
                .amount(amountInMinorUnits)
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
        long amountInMinorUnits = moneyMapper.majorToMinor(command.getAmount());

        if (accountId.equals(command.getTargetAccountId())) {
            throw new BusinessException("Нельзя выполнить перевод на тот же счёт: " + accountId);
        }
        AccountEntity sourceAccount = accountLookupService.findActiveByIdOrThrow(accountId);
        AccountEntity targetAccount = accountLookupService.findActiveByIdOrThrow(command.getTargetAccountId());
        accountBalanceService.ensureSufficientFunds(sourceAccount, amountInMinorUnits);

        UUID operationId = UUID.randomUUID();

        String description = command.getDescription() != null && !command.getDescription().isBlank()
                ? command.getDescription()
                : "Перевод средств";

        return enqueueCrossAccountOperation(
            operationId,
            AccountCommandType.TRANSFER,
            sourceAccount,
            targetAccount,
            amountInMinorUnits,
            description,
            null
        );
    }

    /**
     * Выдать кредит на счёт (пополнение баланса счета).
     */
    public OperationAcceptedResponse loanDisbursement(UUID accountId, LoanDisbursementRequest command) {
        log.debug("loanDisbursement: accountId={}, creditId={}, amount={}", accountId, command.getCreditId(),
                command.getAmount());
        AccountEntity clientAccount = accountLookupService.findActiveByIdOrThrow(accountId);
        AccountEntity masterAccount = bankMasterAccountService.findMasterAccountByIdOrThrow();
        if (clientAccount.getId().equals(masterAccount.getId())) {
            throw new BusinessException("Нельзя выдать кредит на мастер-счёт банка");
        }

        long amountInMinorUnits = moneyMapper.majorToMinor(command.getAmount());
        if (masterAccount.getBalance() < amountInMinorUnits) {
            throw new BusinessException(
                    "У банка недостаточно средств для выдачи кредита. Максимальная сумма кредита: "
                            + moneyMapper.minorToMajor(masterAccount.getBalance()).toPlainString()
                            + " "
                            + accountBalanceService.normalizeCurrency(masterAccount)
            );
        }

        UUID operationId = UUID.randomUUID();

        return enqueueCrossAccountOperation(
                operationId,
                AccountCommandType.LOAN_DISBURSEMENT,
                masterAccount,
                clientAccount,
                amountInMinorUnits,
                command.getDescription() != null ? command.getDescription() : "Выдача кредита",
                command.getCreditId()
        );
    }

    /**
     * Погасить кредит со счёта (снятие со счета).
     */
    public OperationAcceptedResponse loanRepayment(UUID accountId, LoanRepaymentRequest command) {
        log.debug("loanRepayment: accountId={}, creditId={}, amount={}", accountId, command.getCreditId(),
                command.getAmount());
        long amountInMinorUnits = moneyMapper.majorToMinor(command.getAmount());
        AccountEntity clientAccount = accountLookupService.findActiveByIdOrThrow(accountId);
        AccountEntity masterAccount = bankMasterAccountService.findMasterAccountByIdOrThrow();
        accountBalanceService.ensureSufficientFunds(clientAccount, amountInMinorUnits);

        UUID operationId = UUID.randomUUID();

        return enqueueCrossAccountOperation(
                operationId,
                AccountCommandType.LOAN_REPAYMENT,
                clientAccount,
                masterAccount,
                amountInMinorUnits,
                command.getDescription() != null ? command.getDescription() : "Погашение кредита",
                command.getCreditId()
        );
    }

    private OperationAcceptedResponse enqueueCrossAccountOperation(
            UUID operationId,
            AccountCommandType commandType,
            AccountEntity sourceAccount,
            AccountEntity targetAccount,
            long amountInMinorUnits,
            String description,
            UUID creditId
    ) {
        CurrencyConversionService.ConversionQuote conversionQuote = currencyConversionService.quote(
                amountInMinorUnits,
                accountBalanceService.normalizeCurrency(sourceAccount),
                accountBalanceService.normalizeCurrency(targetAccount)
        );

        operationMessageProducer.send(OperationMessage.builder()
                .operationId(operationId)
                .commandType(commandType)
                .sourceAccountId(sourceAccount.getId())
                .targetAccountId(targetAccount.getId())
                .amount(amountInMinorUnits)
                .targetAmount(conversionQuote.targetAmountMinor())
                .sourceCurrency(accountBalanceService.normalizeCurrency(sourceAccount))
                .targetCurrency(accountBalanceService.normalizeCurrency(targetAccount))
                .exchangeRate(conversionQuote.rate())
                .exchangeRateQuotedAt(conversionQuote.quotedAt())
                .description(description)
                .creditId(creditId)
                .build());

        return new OperationAcceptedResponse(operationId, "QUEUED");
    }

}
