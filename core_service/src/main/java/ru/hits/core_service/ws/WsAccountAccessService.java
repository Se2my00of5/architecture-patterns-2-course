package ru.hits.core_service.ws;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import ru.hits.core_service.entity.AccountEntity;
import ru.hits.core_service.repository.AccountRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WsAccountAccessService {

    private final AccountRepository accountRepository;

    public boolean canSubscribe(Authentication authentication, UUID accountId) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        boolean employee = authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_EMPLOYEE".equals(authority.getAuthority()));
        if (employee) {
            return true;
        }

        boolean client = authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_CLIENT".equals(authority.getAuthority()));
        if (!client) {
            return false;
        }

        AccountEntity account = accountRepository.findById(accountId).orElse(null);
        if (account == null) {
            return false;
        }

        String jwtUserId = extractJwtUserId(authentication);
        if (jwtUserId != null) {
            return account.getUserId().toString().equals(jwtUserId);
        }

        return account.getUserId().toString().equals(authentication.getName());
    }

    private String extractJwtUserId(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof Jwt jwt) {
            Object userIdClaim = jwt.getClaims().get("user_id");
            return userIdClaim != null ? userIdClaim.toString() : null;
        }
        return null;
    }
}