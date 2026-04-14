package ru.hits.core_service.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import ru.hits.core_service.entity.enums.CurrencyCode;
import ru.hits.core_service.exception.BusinessException;
import ru.hits.core_service.exception.IntegrationUnavailableException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CbrExchangeRateClient {

    private final RestClient.Builder restClientBuilder;
    private final ObjectMapper objectMapper;
    private final IntegrationCircuitBreaker circuitBreaker;

    @Value("${integration.cbr.base-url:https://www.cbr-xml-daily.ru}")
    private String cbrBaseUrl;

    @Value("${integration.cbr.daily-json-path:/daily_json.js}")
    private String dailyJsonPath;

    @Retryable(
            retryFor = IllegalStateException.class,
            maxAttemptsExpression = "${integration.resilience.retry.max-attempts:3}",
            backoff = @Backoff(
                    delayExpression = "${integration.resilience.retry.delay-ms:300}",
                    multiplierExpression = "${integration.resilience.retry.multiplier:2.0}"
            )
    )
    public Map<CurrencyCode, BigDecimal> fetchBaseCurrencyPerOneUnit() {
        return circuitBreaker.execute("cbr", () -> {
            RestClient restClient = restClientBuilder.baseUrl(cbrBaseUrl).build();

            String payload;
            try {
                payload = restClient.get()
                        .uri(dailyJsonPath)
                        .retrieve()
                        .body(String.class);
            } catch (Exception e) {
                throw new IllegalStateException("Не удалось получить курсы валют ЦБ РФ", e);
            }

            if (payload == null || payload.isBlank()) {
                throw new IllegalStateException("Пустой ответ от API ЦБ РФ");
            }

            try {
                JsonNode root = objectMapper.readTree(payload);
                JsonNode valuteNode = root.path("Valute");

                Map<CurrencyCode, BigDecimal> rates = new EnumMap<>(CurrencyCode.class);
                rates.put(CurrencyCode.RUB, BigDecimal.ONE);
                rates.put(CurrencyCode.USD, extractBaseCurrencyPerUnit(valuteNode, "USD"));
                rates.put(CurrencyCode.CNY, extractBaseCurrencyPerUnit(valuteNode, "CNY"));
                return rates;
            } catch (Exception e) {
                throw new BusinessException("Некорректный формат ответа API ЦБ РФ");
            }
        });
    }

    @Recover
    public Map<CurrencyCode, BigDecimal> recover(IllegalStateException ex) {
        throw new IntegrationUnavailableException("Сервис курсов валют временно недоступен");
    }

    private BigDecimal extractBaseCurrencyPerUnit(JsonNode valuteNode, String code) {
        JsonNode currencyNode = valuteNode.path(code);
        if (currencyNode.isMissingNode()) {
            throw new BusinessException("В ответе ЦБ РФ отсутствует валюта: " + code);
        }

        BigDecimal value = currencyNode.path("Value").decimalValue();
        int nominal = currencyNode.path("Nominal").asInt();
        if (nominal <= 0) {
            throw new BusinessException("Некорректный nominal для валюты: " + code);
        }

        return value.divide(BigDecimal.valueOf(nominal), 10, RoundingMode.HALF_UP);
    }
}