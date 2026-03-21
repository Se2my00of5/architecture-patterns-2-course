package ru.hits.core_service.service;

import org.springframework.stereotype.Service;
import ru.hits.core_service.entity.AccountEntity;
import ru.hits.core_service.entity.enums.CurrencyCode;
import ru.hits.core_service.exception.BusinessException;

@Service
public class AccountBalanceService {

    public void ensureSufficientFunds(AccountEntity account, long amountInMinorUnits) {
        if (account.getBalance() < amountInMinorUnits) {
            throw new BusinessException("Недостаточно средств на счёте: " + account.getId());
        }
    }

    public long decreaseBalance(AccountEntity account, long amountInMinorUnits) {
        ensureSufficientFunds(account, amountInMinorUnits);
        long result = subtractAmounts(account.getBalance(), amountInMinorUnits);
        if (result < 0) {
            throw new BusinessException("Операция привела бы к отрицательному балансу: " + account.getId());
        }
        return result;
    }

    public long addAmounts(long left, long right) {
        try {
            return Math.addExact(left, right);
        } catch (ArithmeticException e) {
            throw new BusinessException("Переполнение при расчёте суммы");
        }
    }

    public long subtractAmounts(long left, long right) {
        try {
            return Math.subtractExact(left, right);
        } catch (ArithmeticException e) {
            throw new BusinessException("Переполнение при расчёте суммы");
        }
    }

    public CurrencyCode normalizeCurrency(AccountEntity account) {
        return account.getCurrency() != null ? account.getCurrency() : CurrencyCode.RUB;
    }
}