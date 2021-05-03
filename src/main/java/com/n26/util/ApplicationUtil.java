package com.n26.util;

import com.n26.exception.InvalidRequestException;
import com.n26.exception.UnrecognizedDataFormatException;
import com.n26.model.TransactionVO;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.time.Instant;

public interface ApplicationUtil {
    int ONE_SECOND_IN_MILLIS = 1000;
    long TEN_SECOND_IN_MILLIS = 10*ONE_SECOND_IN_MILLIS;
    String VALID_DECIMAL_NUMBER_STRING = "^\\d*\\.\\d+|\\d+\\.\\d*$";
    String APPLICATION_PATH_GET_STATISTICS = "/statistics";
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

    static boolean isPastMinuteTimeStamp(Long timestamp) {
        long now = Instant.now().getEpochSecond();
        return timestamp < now-60;
    }

    static long getTimeDifferenceFromNow(String dateString) {
        long now = getCurrentTimeInUTC() / ONE_SECOND_IN_MILLIS;
        long timeInSeconds = Instant.parse(dateString).getEpochSecond();
        return now - timeInSeconds;
    }

    static void validateTransactionData(TransactionVO txn) {
        if(txn.getAmount().isEmpty() || !txn.getAmount().matches(VALID_DECIMAL_NUMBER_STRING) || txn.getTimestamp().isEmpty()) throw new InvalidRequestException();
        try{
            Instant.parse(txn.getTimestamp());
        }catch (Exception ex) {
            throw new UnrecognizedDataFormatException();
        }
    }

}
