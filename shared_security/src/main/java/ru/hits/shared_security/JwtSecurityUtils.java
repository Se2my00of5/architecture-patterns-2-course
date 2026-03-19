package ru.hits.shared_security;

import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

public final class JwtSecurityUtils {

    private JwtSecurityUtils() {
    }

    public static JwtAuthenticationConverter jwtAuthenticationConverter() {
        var authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthorityPrefix("ROLE_");
        authoritiesConverter.setAuthoritiesClaimName("roles");

        var converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        return converter;
    }

    public static RSAPublicKey loadPublicKey(String publicKeyPem) {
        if (!StringUtils.hasText(publicKeyPem)) {
            throw new IllegalStateException("security.jwt.public-key is not set");
        }

        try (ByteArrayInputStream inputStream =
                     new ByteArrayInputStream(normalizePem(publicKeyPem).getBytes(StandardCharsets.UTF_8))) {
            return RsaKeyConverters.x509().convert(inputStream);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to load RSA public key from security.jwt.public-key", ex);
        }
    }

    public static RSAPrivateKey loadPrivateKey(String privateKeyPem) {
        if (!StringUtils.hasText(privateKeyPem)) {
            throw new IllegalStateException("security.jwt.private-key is not set");
        }

        try (ByteArrayInputStream inputStream =
                     new ByteArrayInputStream(normalizePem(privateKeyPem).getBytes(StandardCharsets.UTF_8))) {
            return RsaKeyConverters.pkcs8().convert(inputStream);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to load RSA private key from security.jwt.private-key", ex);
        }
    }

    private static String normalizePem(String pem) {
        return pem.replace("\\n", "\n").trim();
    }
}
