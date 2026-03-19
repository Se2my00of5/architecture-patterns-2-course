package ru.hits.user_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import ru.hits.user_service.dto.response.AuthResponse;
import ru.hits.user_service.entity.UserEntity;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JwtTokenService {

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

    private static final long ACCESS_TOKEN_TTL = 900; // 15 минут
    private static final long REFRESH_TOKEN_TTL = 604800; // 7 дней

    public AuthResponse generateTokens(UserEntity user) {
        Instant now = Instant.now();
        Instant accessTokenExpiresAt = now.plus(ACCESS_TOKEN_TTL, ChronoUnit.SECONDS);
        Instant refreshTokenExpiresAt = now.plus(REFRESH_TOKEN_TTL, ChronoUnit.SECONDS);

        String roles = user.getRoles() != null && !user.getRoles().isEmpty()
                ? user.getRoles().stream()
                    .map(Enum::name)
                    .collect(Collectors.joining(","))
                : "CLIENT";

        // Access token
        JwtClaimsSet accessTokenClaims = JwtClaimsSet.builder()
                .issuedAt(now)
                .expiresAt(accessTokenExpiresAt)
                .claim("roles", roles)
                .claim("user_id", user.getId().toString())
                .claim("full_name", user.getFullName())
                .claim("login", user.getLogin())
                .build();

        String accessToken = jwtEncoder.encode(JwtEncoderParameters.from(accessTokenClaims)).getTokenValue();

        // Refresh token
        JwtClaimsSet refreshTokenClaims = JwtClaimsSet.builder()
                .issuedAt(now)
                .expiresAt(refreshTokenExpiresAt)
                .claim("type", "refresh")
                .build();

        String refreshToken = jwtEncoder.encode(JwtEncoderParameters.from(refreshTokenClaims)).getTokenValue();

        return AuthResponse.builder()
                .accessToken(accessToken)
                .expiresIn(ACCESS_TOKEN_TTL)
                .refreshToken(refreshToken)
                .build();
    }

    public Jwt decodeRefreshToken(String refreshToken) {
        return jwtDecoder.decode(refreshToken);
    }
}
