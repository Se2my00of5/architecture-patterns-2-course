package ru.hits.core_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.hits.core_service.entity.AccountEntity;
import ru.hits.core_service.entity.enums.AccountStatus;
import ru.hits.core_service.exception.BusinessException;
import ru.hits.core_service.exception.NotFoundException;
import ru.hits.core_service.repository.AccountRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountLookupService {

    private final AccountRepository accountRepository;

    public AccountEntity findActiveByIdOrThrow(UUID accountId) {
        AccountEntity account = accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Счёт не найден: " + accountId));
        ensureActive(account);
        return account;
    }

    public AccountEntity findActiveByIdForUpdateOrThrow(UUID accountId) {
        AccountEntity account = accountRepository.findByIdForUpdate(accountId)
                .orElseThrow(() -> new NotFoundException("Счёт не найден: " + accountId));
        ensureActive(account);
        return account;
    }

    private void ensureActive(AccountEntity account) {
        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new BusinessException("Счёт закрыт: " + account.getId());
        }
    }
}