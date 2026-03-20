package ru.hits.core_service.mapper;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class MoneyMapper {

    public BigDecimal kopecksToRubles(Long amountInKopecks) {
        if (amountInKopecks == null) {
            return null;
        }
        return BigDecimal.valueOf(amountInKopecks, 2).setScale(2, RoundingMode.UNNECESSARY);
    }

    public Long rublesToKopecks(BigDecimal amountInRubles) {
        if (amountInRubles == null) {
            return null;
        }
        return amountInRubles
                .movePointRight(2)
                .setScale(0, RoundingMode.UNNECESSARY)
                .longValueExact();
    }
}
