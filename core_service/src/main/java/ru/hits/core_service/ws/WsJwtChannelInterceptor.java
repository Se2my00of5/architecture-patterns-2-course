package ru.hits.core_service.ws;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.stereotype.Component;
import ru.hits.shared_security.JwtSecurityUtils;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class WsJwtChannelInterceptor implements ChannelInterceptor {

    private final JwtDecoder jwtDecoder;
    private final WsAccountAccessService wsAccountAccessService;
    private final JwtAuthenticationConverter jwtAuthenticationConverter = JwtSecurityUtils.jwtAuthenticationConverter();

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        StompCommand command = accessor.getCommand();

        if (StompCommand.CONNECT.equals(command)) {
            String bearerToken = resolveBearer(accessor);
            if (bearerToken == null) {
                throw new MessagingException("WS CONNECT requires Bearer token");
            }

            Jwt jwt = jwtDecoder.decode(bearerToken);
            var authentication = jwtAuthenticationConverter.convert(jwt);
            if (authentication == null) {
                throw new MessagingException("WS JWT authentication failed");
            }

            accessor.setUser(authentication);
            return message;
        }

        if (StompCommand.SUBSCRIBE.equals(command)) {
            var user = accessor.getUser();
            if (!(user instanceof org.springframework.security.core.Authentication authentication)) {
                throw new MessagingException("WS SUBSCRIBE requires authenticated user");
            }

            UUID accountId = extractAccountId(accessor.getDestination());
            if (accountId == null) {
                throw new MessagingException("Invalid subscribe destination");
            }

            if (!wsAccountAccessService.canSubscribe(authentication, accountId)) {
                throw new MessagingException("Access denied for account subscription: " + accountId);
            }
        }

        return message;
    }

    private String resolveBearer(StompHeaderAccessor accessor) {
        String authHeader = accessor.getFirstNativeHeader("Authorization");
        if (authHeader == null) {
            authHeader = accessor.getFirstNativeHeader("authorization");
        }
        if (authHeader == null) {
            return null;
        }
        if (!authHeader.startsWith("Bearer ")) {
            return null;
        }
        return authHeader.substring("Bearer ".length()).trim();
    }

    private UUID extractAccountId(String destination) {
        if (destination == null) {
            return null;
        }
        String prefix = "/topic/accounts/";
        String suffix = "/operations";
        if (!destination.startsWith(prefix) || !destination.endsWith(suffix)) {
            return null;
        }

        String accountIdRaw = destination.substring(prefix.length(), destination.length() - suffix.length());
        try {
            return UUID.fromString(accountIdRaw);
        } catch (IllegalArgumentException e) {
            log.debug("Invalid accountId in WS destination: {}", destination);
            return null;
        }
    }
}