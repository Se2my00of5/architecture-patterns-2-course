package ru.hits.core_service.controller.command;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.hits.core_service.dto.request.DepositRequest;
import ru.hits.core_service.dto.request.OpenAccountRequest;
import ru.hits.core_service.dto.request.WithdrawRequest;
import ru.hits.core_service.dto.response.AccountResponse;
import ru.hits.core_service.handler.command.AccountCommandHandler;

import java.util.UUID;


@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Tag(name = "Счета — Команды")
public class AccountCommandController {

    private final AccountCommandHandler commandHandler;

    @PostMapping
    @Operation(summary = "Открыть новый счёт для клиента")
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse openAccount(@Valid @RequestBody OpenAccountRequest request) {
        return commandHandler.openAccount(request);
    }

    @PostMapping("/{accountId}/close")
    @Operation(summary = "Закрыть счёт")
    @ResponseStatus(HttpStatus.OK)
    public AccountResponse closeAccount(@PathVariable UUID accountId) {
        return commandHandler.closeAccount(accountId);
    }

    @PostMapping("/{accountId}/deposit")
    @Operation(summary = "Внести деньги на счёт")
    @ResponseStatus(HttpStatus.OK)
    public AccountResponse deposit(
            @PathVariable UUID accountId, @Valid @RequestBody DepositRequest request
    ) {
        return commandHandler.deposit(accountId, request);
    }

    @PostMapping("/{accountId}/withdraw")
    @Operation(summary = "Снять деньги со счёта")
    @ResponseStatus(HttpStatus.OK)
    public AccountResponse withdraw(
            @PathVariable UUID accountId, @Valid @RequestBody WithdrawRequest request
    ) {
        return commandHandler.withdraw(accountId, request);
    }
}
