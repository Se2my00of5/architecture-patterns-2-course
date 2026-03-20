package ru.hits.user_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

import java.security.interfaces.RSAPublicKey;
import ru.hits.shared_security.JwtSecurityUtils;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(authorize -> authorize
                    .requestMatchers("/api/auth/register").permitAll()
                        .requestMatchers("/login").permitAll()
                        // Swagger
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        // User endpoints (требуют аутентификации)
                        .requestMatchers("/api/users/**").authenticated()
                        // Всё остальное требует аутентификации
                        .anyRequest().authenticated()
                )
                .formLogin(Customizer.withDefaults())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(JwtSecurityUtils.jwtAuthenticationConverter()))
                );

        return http.build();
    }


    /**
     * Валидирует входящие JWT токены.
     * Проверяет подпись и срок действия, извлекает claims.
     */
    @Bean
    public JwtDecoder jwtDecoder(
            @Value("${security.jwt.public-key}") String publicKeyPem
    ) {
        RSAPublicKey publicKey = JwtSecurityUtils.loadPublicKey(publicKeyPem);
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(publicKey).build();

        OAuth2TokenValidator<Jwt> defaultValidator = JwtValidators.createDefault();
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(defaultValidator));

        return decoder;
    }
}
