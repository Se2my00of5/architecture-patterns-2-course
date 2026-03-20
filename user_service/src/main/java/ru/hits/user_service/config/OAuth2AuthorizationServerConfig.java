package ru.hits.user_service.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import ru.hits.shared_security.JwtSecurityUtils;
import ru.hits.user_service.entity.UserEntity;
import ru.hits.user_service.repository.UserRepository;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
public class OAuth2AuthorizationServerConfig {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) {
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer = new OAuth2AuthorizationServerConfigurer();
        RequestMatcher authorizationServerEndpointsMatcher = authorizationServerConfigurer.getEndpointsMatcher();

        http
                .securityMatcher(authorizationServerEndpointsMatcher)
                .with(authorizationServerConfigurer, Customizer.withDefaults())
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated())
                .exceptionHandling(exceptions -> exceptions
                        .defaultAuthenticationEntryPointFor(
                                new LoginUrlAuthenticationEntryPoint("/login"),
                                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
                        )
                )
                .oauth2ResourceServer(resourceServer -> resourceServer.jwt(Customizer.withDefaults()));

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            UserEntity userEntity = userRepository.findByLogin(username)
                    .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

            if (userEntity.getIsBlocked()) {
                throw new IllegalStateException("Пользователь заблокирован");
            }

            Set<GrantedAuthority> authorities = userEntity.getRoles().stream()
                    .map(role -> "ROLE_" + role.name())
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toSet());

            return User.withUsername(userEntity.getLogin())
                    .password(userEntity.getPasswordHash())
                    .authorities(authorities)
                    .accountLocked(userEntity.getIsBlocked())
                    .build();
        };
    }

    @Bean
    public AuthenticationProvider daoAuthenticationProvider(UserDetailsService userDetailsService) {
        var provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public RegisteredClientRepository registeredClientRepository(
            @Value("${oauth2.auth-server.clients.frontend.client-id}") String frontendClientId,
            @Value("${oauth2.auth-server.clients.frontend.client-secret}") String frontendClientSecret,
            @Value("${oauth2.auth-server.clients.frontend.redirect-uri}") String frontendRedirectUri,
            @Value("${oauth2.auth-server.clients.service.client-id}") String serviceClientId,
            @Value("${oauth2.auth-server.clients.service.client-secret}") String serviceClientSecret
    ) {
        RegisteredClient frontendClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId(frontendClientId)
                .clientSecret(passwordEncoder.encode(frontendClientSecret))
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri(frontendRedirectUri)
                .scope("api")
                .clientSettings(ClientSettings.builder()
                        .requireProofKey(true)
                        .build())
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofMinutes(15))
                        .refreshTokenTimeToLive(Duration.ofDays(7))
                        .reuseRefreshTokens(false)
                        .build())
                .build();

        RegisteredClient serviceClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId(serviceClientId)
                .clientSecret(passwordEncoder.encode(serviceClientSecret))
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .scope("api")
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofMinutes(10))
                        .build())
                .build();

        return new InMemoryRegisteredClientRepository(frontendClient, serviceClient);
    }

    @Bean
    public OAuth2AuthorizationService authorizationService() {
        return new InMemoryOAuth2AuthorizationService();
    }

    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer() {
        return context -> {
            if (!OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
                return;
            }

            if (AuthorizationGrantType.CLIENT_CREDENTIALS.equals(context.getAuthorizationGrantType())) {
                context.getClaims().claim("service", true);
                context.getClaims().claim("client_id", context.getRegisteredClient().getClientId());
                context.getClaims().claim("roles", "SERVICE");
                return;
            }

            String login = context.getPrincipal().getName();
            UserEntity user = userRepository.findByLogin(login)
                    .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден: " + login));

            String roles = user.getRoles() == null || user.getRoles().isEmpty()
                    ? "CLIENT"
                    : user.getRoles().stream().map(Enum::name).collect(Collectors.joining(","));

            context.getClaims().claim("login", user.getLogin());
            context.getClaims().claim("user_id", user.getId().toString());
            context.getClaims().claim("full_name", user.getFullName());
            context.getClaims().claim("roles", roles);
        };
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource(
            @Value("${security.jwt.public-key}") String publicKeyPem,
            @Value("${security.jwt.private-key}") String privateKeyPem
    ) {
        RSAPublicKey publicKey = JwtSecurityUtils.loadPublicKey(publicKeyPem);
        RSAPrivateKey privateKey = JwtSecurityUtils.loadPrivateKey(privateKeyPem);

        var rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(UUID.randomUUID().toString())
                .build();

        JWKSet jwkSet = new JWKSet(rsaKey);
        return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings(
            @Value("${oauth2.auth-server.issuer-uri:http://localhost:1115}") String issuerUri
    ) {
        return AuthorizationServerSettings.builder()
                .issuer(issuerUri)
                .build();
    }
}
