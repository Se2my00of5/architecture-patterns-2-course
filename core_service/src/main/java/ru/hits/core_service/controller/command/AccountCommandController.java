package ru.hits.core_service.controller.command;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.hits.shared_resilience.jwt.JwtScopeResolver;
import ru.hits.core_service.dto.request.DepositRequest;
import ru.hits.core_service.dto.request.LoanDisbursementRequest;
import ru.hits.core_service.dto.request.LoanRepaymentRequest;
import ru.hits.core_service.dto.request.OpenAccountRequest;
import ru.hits.core_service.dto.request.TransferRequest;
import ru.hits.core_service.dto.request.WithdrawRequest;
import ru.hits.core_service.dto.response.AccountResponse;
import ru.hits.core_service.dto.response.OperationAcceptedResponse;
import ru.hits.core_service.handler.command.AccountCommandHandler;
import ru.hits.core_service.service.IdempotencyService;

import java.util.UUID;


@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Tag(name = "Счета — Команды")
public class AccountCommandController {

    private final AccountCommandHandler commandHandler;
    private final IdempotencyService idempotencyService;

    @PostMapping
    @Operation(summary = "Открыть новый счёт для клиента")
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse openAccount(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody OpenAccountRequest request,
            HttpServletRequest httpRequest
    ) {
        return idempotencyService.execute(
                JwtScopeResolver.resolveUserScope(jwt, "accounts:anonymous"),
                idempotencyKey,
                httpRequest.getMethod(),
                httpRequest.getRequestURI(),
                request,
                AccountResponse.class,
                () -> commandHandler.openAccount(request)
        );
    }

    @PostMapping("/{accountId}/close")
    @Operation(summary = "Закрыть счёт")
    @ResponseStatus(HttpStatus.OK)
    public AccountResponse closeAccount(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @PathVariable UUID accountId,
            HttpServletRequest httpRequest
    ) {
        return idempotencyService.execute(
                JwtScopeResolver.resolveUserScope(jwt, "accounts:anonymous"),
                idempotencyKey,
                httpRequest.getMethod(),
                httpRequest.getRequestURI(),
                accountId,
                AccountResponse.class,
                () -> commandHandler.closeAccount(accountId)
        );
    }

    @PostMapping("/{accountId}/deposit")
    @Operation(summary = "Внести деньги на счёт")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public OperationAcceptedResponse deposit(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @PathVariable UUID accountId,
            @Valid @RequestBody DepositRequest request,
            HttpServletRequest httpRequest
    ) {
        return idempotencyService.execute(
                JwtScopeResolver.resolveUserScope(jwt, "accounts:anonymous"),
                idempotencyKey,
                httpRequest.getMethod(),
                httpRequest.getRequestURI(),
                request,
                OperationAcceptedResponse.class,
                () -> commandHandler.deposit(accountId, request)
        );
    }

    @PostMapping("/{accountId}/withdraw")
    @Operation(summary = "Снять деньги со счёта")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public OperationAcceptedResponse withdraw(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @PathVariable UUID accountId,
            @Valid @RequestBody WithdrawRequest request,
            HttpServletRequest httpRequest
    ) {
        return idempotencyService.execute(
                JwtScopeResolver.resolveUserScope(jwt, "accounts:anonymous"),
                idempotencyKey,
                httpRequest.getMethod(),
                httpRequest.getRequestURI(),
                request,
                OperationAcceptedResponse.class,
                () -> commandHandler.withdraw(accountId, request)
        );
    }

    @PostMapping("/{accountId}/transfer")
    @Operation(summary = "Перевести деньги на другой счёт")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public OperationAcceptedResponse transfer(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @PathVariable UUID accountId,
            @Valid @RequestBody TransferRequest request,
            HttpServletRequest httpRequest
    ) {
        return idempotencyService.execute(
                JwtScopeResolver.resolveUserScope(jwt, "accounts:anonymous"),
                idempotencyKey,
                httpRequest.getMethod(),
                httpRequest.getRequestURI(),
                request,
                OperationAcceptedResponse.class,
                () -> commandHandler.transfer(accountId, request)
        );
    }

    @PostMapping("/{accountId}/loan-disbursement")
    @Operation(summary = "Выдать кредит на счёт (внутренний, для взаимодействия)")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public OperationAcceptedResponse loanDisbursement(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @PathVariable UUID accountId,
            @Valid @RequestBody LoanDisbursementRequest request,
            HttpServletRequest httpRequest
    ) {
        return idempotencyService.execute(
                JwtScopeResolver.resolveUserScope(jwt, "accounts:anonymous"),
                idempotencyKey,
                httpRequest.getMethod(),
                httpRequest.getRequestURI(),
                request,
                OperationAcceptedResponse.class,
                () -> commandHandler.loanDisbursement(accountId, request)
        );
    }

    @PostMapping("/{accountId}/loan-repayment")
    @Operation(summary = "Погасить кредит со счёта (внутренний, для взаимодействия)")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public OperationAcceptedResponse loanRepayment(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @PathVariable UUID accountId,
            @Valid @RequestBody LoanRepaymentRequest request,
            HttpServletRequest httpRequest
    ) {
        return idempotencyService.execute(
                JwtScopeResolver.resolveUserScope(jwt, "accounts:anonymous"),
                idempotencyKey,
                httpRequest.getMethod(),
                httpRequest.getRequestURI(),
                request,
                OperationAcceptedResponse.class,
                () -> commandHandler.loanRepayment(accountId, request)
        );
    }
}
