package ru.hits.shared_resilience.instability;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

public final class InstabilityDecider {

    private InstabilityDecider() {
    }

    public static boolean shouldFail(double defaultErrorRate, double evenMinuteErrorRate) {
        int minute = LocalDateTime.now().getMinute();
        double errorRate = minute % 2 == 0 ? evenMinuteErrorRate : defaultErrorRate;
        return ThreadLocalRandom.current().nextDouble() < errorRate;
    }
}
