package ru.hits.user_service.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.hits.shared_resilience.instability.InstabilityDecider;
import ru.hits.shared_resilience.exception.SimulatedServiceFailureException;

import java.io.IOException;

@Component
public class ProbabilisticFailureFilter extends OncePerRequestFilter {

    @Value("${app.instability.enabled:true}")
    private boolean enabled;

    @Value("${app.instability.default-error-rate:0.3}")
    private double defaultErrorRate;

    @Value("${app.instability.even-minute-error-rate:0.7}")
    private double evenMinuteErrorRate;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (enabled) {
            if (InstabilityDecider.shouldFail(defaultErrorRate, evenMinuteErrorRate)) {
                throw new SimulatedServiceFailureException("Сервис временно нестабилен (симуляция ошибки)");
            }
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs");
    }
}
