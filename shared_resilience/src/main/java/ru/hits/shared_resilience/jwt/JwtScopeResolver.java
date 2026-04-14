package ru.hits.shared_resilience.jwt;

import org.springframework.security.oauth2.jwt.Jwt;

public final class JwtScopeResolver {

    private JwtScopeResolver() {
    }

    public static String resolveUserScope(Jwt jwt, String fallbackScope) {
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
