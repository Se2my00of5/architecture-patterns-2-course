package ru.hits.core_service.mapper;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class MoneyMapper {

    public BigDecimal minorToMajor(Long amountInMinorUnits) {
        if (amountInMinorUnits == null) {
            return null;
        }
        return BigDecimal.valueOf(amountInMinorUnits, 2).setScale(2, RoundingMode.UNNECESSARY);
    }

    public Long majorToMinor(BigDecimal amountInMajorUnits) {
        if (amountInMajorUnits == null) {
            return null;
        }
        return amountInMajorUnits
                .movePointRight(2)
                .setScale(0, RoundingMode.UNNECESSARY)
                .longValueExact();
    }
}
