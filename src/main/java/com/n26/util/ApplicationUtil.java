package com.n26.util;

import java.math.BigDecimal;
import java.time.Instant;

public interface ApplicationUtil {
    int ONE_SECOND_IN_MILLIS = 1000;
    static long getCurrentTimeInUTC() {
        return Instant.now().toEpochMilli();
    }

    static boolean isFutureDate(String dateString) {
        long now = getCurrentTimeInUTC();
        return now < Instant.parse(dateString).toEpochMilli();
    }

    static boolean isPastMinuteTime(String dateString) {
        long difference = getTimeDifferenceFromNow(dateString);
        return difference >= 60;
    }

    static long getTimeDifferenceFromNow(String dateString) {
        long now = getCurrentTimeInUTC() / ONE_SECOND_IN_MILLIS;
        long timeInSeconds = Instant.parse(dateString).getEpochSecond();
        return now - timeInSeconds;
    }

}