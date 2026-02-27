package ru.hits.core_service.handler.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import ru.hits.core_service.dto.response.AccountResponse;
import ru.hits.core_service.dto.response.OperationResponse;
import ru.hits.core_service.entity.AccountEntity;
import ru.hits.core_service.exception.NotFoundException;
import ru.hits.core_service.mapper.AccountMapper;
import ru.hits.core_service.mapper.OperationMapper;
import ru.hits.core_service.repository.AccountRepository;
import ru.hits.core_service.repository.OperationRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AccountQueryHandler {

    private final AccountRepository accountRepository;
    private final OperationRepository operationRepository;
    private final AccountMapper accountMapper;
    private final OperationMapper operationMapper;

    /**
     * Получить все счета конкретного клиента.
     */
    public List<AccountResponse> getAccountsByUser(UUID userId) {
        return accountMapper.toResponseList(accountRepository.findByUserId(userId));
    }

    /**
     * Получить все счета (для сотрудника банка).
     */
    public List<AccountResponse> getAllAccounts() {
        return accountMapper.toResponseList(accountRepository.findAll());
    }

    /**
     * Получить информацию о конкретном счёте.
     */
    public AccountResponse getAccount(UUID accountId) {
        AccountEntity account = accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Счёт не найден: " + accountId));
        return accountMapper.toResponse(account);
    }

    /**
     * Получить историю операций по счёту.
     */
    public List<OperationResponse> getAccountOperations(UUID accountId) {
        if (!accountRepository.existsById(accountId)) {
            throw new NotFoundException("Счёт не найден: " + accountId);
        }
        return operationMapper.toResponseList(
                operationRepository.findByAccountIdOrderByCreatedAtDesc(accountId));
    }
}
