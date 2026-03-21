package ru.hits.core_service.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.hits.core_service.entity.enums.AccountStatus;
import ru.hits.core_service.entity.enums.CurrencyCode;
import ru.hits.core_service.exception.BusinessException;
import ru.hits.core_service.repository.AccountRepository;

import java.util.UUID;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final AccountRepository accountRepository;

    @Bean
    public ApplicationRunner initBankMasterAccount(
            @Value("${app.bank.master-account-id:00000000-0000-0000-0000-000000000001}") String masterAccountIdRaw,
            @Value("${app.bank.master-account-initial-balance-minor:1000000000}") long initialBalanceMinor
    ) {
        return args -> {
            UUID masterAccountId = parseUuidOrThrow(masterAccountIdRaw, "app.bank.master-account-id");
            UUID masterUserId = parseUuidOrThrow("00000000-0000-0000-0000-000000000000", "app.bank.master-user-id");

            if (initialBalanceMinor < 0) {
                throw new BusinessException("Начальный баланс мастер-счёта не может быть отрицательным");
            }

            int inserted = accountRepository.insertMasterAccountIfMissing(
                    masterAccountId,
                    masterUserId,
                    initialBalanceMinor,
                    CurrencyCode.RUB.name(),
                    AccountStatus.ACTIVE.name()
            );

            if (inserted > 0) {
                log.info("Master account initialized: {}, technical userId={}", masterAccountId, masterUserId);
            } else {
                log.debug("Master account already exists: {}", masterAccountId);
            }
        };
    }

    private UUID parseUuidOrThrow(String value, String propertyName) {
        try {
            return UUID.fromString(value);
        } catch (Exception e) {
            throw new BusinessException("Некорректный UUID в " + propertyName);
        }
    }
}