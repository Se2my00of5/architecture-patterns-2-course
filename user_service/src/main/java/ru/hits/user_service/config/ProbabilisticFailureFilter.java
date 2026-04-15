package ru.hits.user_service.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.hits.shared_resilience.instability.InstabilityDecider;
import ru.hits.shared_resilience.exception.SimulatedServiceFailureException;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class ProbabilisticFailureFilter extends OncePerRequestFilter {

    private final HandlerExceptionResolver handlerExceptionResolver;

    @Value("${app.instability.enabled:true}")
    private boolean enabled;

    @Value("${app.instability.default-error-rate:0.3}")
    private double defaultErrorRate;

    @Value("${app.instability.even-minute-error-rate:0.7}")
    private double evenMinuteErrorRate;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (enabled && InstabilityDecider.shouldFail(defaultErrorRate, evenMinuteErrorRate)) {
            handlerExceptionResolver.resolveException(
                    request,
                    response,
                    null,
                    new SimulatedServiceFailureException("Сервис временно нестабилен (симуляция ошибки)")
            );
            return;
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs");
    }
}
