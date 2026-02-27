package ru.hits.core_service.controller.query;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.hits.core_service.dto.response.AccountResponse;
import ru.hits.core_service.dto.response.OperationResponse;
import ru.hits.core_service.handler.query.AccountQueryHandler;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Tag(name = "Счета — Запросы")
public class AccountQueryController {

    private final AccountQueryHandler queryHandler;

    @GetMapping("/user/{userId}")
    @Operation(summary = "Получить все счета клиента")
    @ResponseStatus(HttpStatus.OK)
    public List<AccountResponse> getAccountsByUser(@PathVariable UUID userId) {
        return queryHandler.getAccountsByUser(userId);
    }

    @GetMapping
    @Operation(summary = "Получить все счета всех клиентов")
    @ResponseStatus(HttpStatus.OK)
    public List<AccountResponse> getAllAccounts() {
        return queryHandler.getAllAccounts();
    }

    @GetMapping("/{accountId}")
    @Operation(summary = "Получить информацию о конкретном счёте")
    @ResponseStatus(HttpStatus.OK)
    public AccountResponse getAccount(@PathVariable UUID accountId) {
        return queryHandler.getAccount(accountId);
    }

    @GetMapping("/{accountId}/operations")
    @Operation(summary = "Получить историю операций по счёту")
    @ResponseStatus(HttpStatus.OK)
    public List<OperationResponse> getAccountOperations(@PathVariable UUID accountId) {
        return queryHandler.getAccountOperations(accountId);
    }
}
