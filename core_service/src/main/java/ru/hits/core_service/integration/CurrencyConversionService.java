package ru.hits.core_service.integration;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.hits.core_service.entity.enums.CurrencyCode;
import ru.hits.core_service.exception.BusinessException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.EnumMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CurrencyConversionService {

    private final CbrExchangeRateClient cbrExchangeRateClient;

    @Value("${integration.cbr.cache-ttl-seconds:3600}")
    private long cacheTtlSeconds;

    private volatile Map<CurrencyCode, BigDecimal> cachedBaseCurrencyPerUnit = new EnumMap<>(CurrencyCode.class);
    private volatile Instant cacheExpiresAt = Instant.EPOCH;

    public ConversionQuote quote(long sourceAmountMinor, CurrencyCode sourceCurrency, CurrencyCode targetCurrency) {
        if (sourceAmountMinor <= 0) {
            throw new BusinessException("Сумма конвертации должна быть положительной");
        }

        LocalDateTime quotedAt = LocalDateTime.now(ZoneOffset.UTC);

        if (sourceCurrency == targetCurrency) {
            return new ConversionQuote(sourceAmountMinor, BigDecimal.ONE, quotedAt);
        }

        Map<CurrencyCode, BigDecimal> baseCurrencyPerUnit = getBaseCurrencyPerUnit();
        BigDecimal sourceRateToBase = baseCurrencyPerUnit.get(sourceCurrency);
        BigDecimal targetRateToBase = baseCurrencyPerUnit.get(targetCurrency);

        if (sourceRateToBase == null || targetRateToBase == null) {
            throw new BusinessException("Не удалось получить курс для конвертации " + sourceCurrency + " -> " + targetCurrency);
        }

        // Кросс-курс через базовую валюту ЦБ: (source -> base) / (target -> base).
        BigDecimal rate = sourceRateToBase.divide(targetRateToBase, 10, RoundingMode.HALF_UP);

        // Внутри сервиса суммы храним в минимальных единицах (2 знака),
        // поэтому сначала считаем в major-формате, затем возвращаем обратно в minor.
        BigDecimal sourceMajor = BigDecimal.valueOf(sourceAmountMinor, 2);
        BigDecimal targetMajor = sourceMajor.multiply(rate);

        long targetAmountMinor;
        try {
            targetAmountMinor = targetMajor
                    .movePointRight(2)
                    // Округляем до целой минимальной единицы целевой валюты.
                    .setScale(0, RoundingMode.HALF_UP)
                    .longValueExact();
        } catch (ArithmeticException e) {
            throw new BusinessException("Ошибка округления при конвертации валют");
        }

        if (targetAmountMinor <= 0) {
            throw new BusinessException("Сконвертированная сумма должна быть положительной");
        }

        return new ConversionQuote(targetAmountMinor, rate, quotedAt);
    }

    private synchronized Map<CurrencyCode, BigDecimal> getBaseCurrencyPerUnit() {
        Instant now = Instant.now();
        if (!cachedBaseCurrencyPerUnit.isEmpty() && now.isBefore(cacheExpiresAt)) {
            return cachedBaseCurrencyPerUnit;
        }

        Map<CurrencyCode, BigDecimal> freshRates = cbrExchangeRateClient.fetchBaseCurrencyPerOneUnit();
        cachedBaseCurrencyPerUnit = new EnumMap<>(freshRates);
        cacheExpiresAt = now.plusSeconds(Math.max(cacheTtlSeconds, 30));
        return cachedBaseCurrencyPerUnit;
    }

    public record ConversionQuote(long targetAmountMinor, BigDecimal rate, LocalDateTime quotedAt) {
    }
}