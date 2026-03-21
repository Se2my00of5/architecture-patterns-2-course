package ru.hits.core_service.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.hits.core_service.entity.AccountEntity;
import ru.hits.core_service.exception.BusinessException;

import java.util.UUID;

@Service
public class BankMasterAccountService {

    private final AccountLookupService accountLookupService;
    private final String masterAccountIdRaw;

    public BankMasterAccountService(
            AccountLookupService accountLookupService,
            @Value("${app.bank.master-account-id:}") String masterAccountIdRaw) {
        this.accountLookupService = accountLookupService;
        this.masterAccountIdRaw = masterAccountIdRaw;
    }

    public AccountEntity findMasterAccountByIdOrThrow() {
        return accountLookupService.findActiveByIdOrThrow(getMasterAccountId());
    }

    private UUID getMasterAccountId() {
        if (masterAccountIdRaw == null || masterAccountIdRaw.isBlank()) {
            throw new BusinessException("Не настроен мастер-счёт банка (app.bank.master-account-id)");
        }

        try {
            return UUID.fromString(masterAccountIdRaw);
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Некорректный UUID мастер-счёта банка (app.bank.master-account-id)");
        }
    }
}