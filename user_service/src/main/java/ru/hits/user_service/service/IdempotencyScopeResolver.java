package ru.hits.user_service.service;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class IdempotencyScopeResolver {

    public String resolveUserScope(Jwt jwt, String fallbackScope) {
        if (jwt == null) {
            return fallbackScope;
        }

        String login = jwt.getClaimAsString("login");
        if (login != null && !login.isBlank()) {
            return "user:" + login;
        }

        String sub = jwt.getSubject();
        if (sub != null && !sub.isBlank()) {
            return "sub:" + sub;
        }

        return fallbackScope;
    }
}
