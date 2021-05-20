package com.n26.util;

import com.n26.exception.UnrecognizedDataFormatException;
import com.n26.model.TransactionVO;

import java.math.BigDecimal;
import java.time.Instant;

public interface ApplicationUtil {
    int ONE_SECOND_IN_MILLIS = 1000;
    long TEN_SECOND_IN_MILLIS = 10 * ONE_SECOND_IN_MILLIS;
    String VALID_DECIMAL_NUMBER_STRING_REGEX = "^\\d*\\.\\d+|\\d+\\.\\d*|\\d+$";
    int TWO_DECIMAL_SCALE = 2;
    String AMOUNT_ZERO_STRING = "0.00";
    String APPLICATION_PATH_GET_STATISTICS = "/statistics";
    String APPLICATION_PATH_TRANSACTIONS = "/transactions";

    /**
     * Method gets the ZERO big decimal with decimal point precisions is set to 2 places for the converted value
     *
     * @return ZERO valued big decimal with scaled precision
     * */
    static BigDecimal get2ScaledBigDecimal() {
        return get2ScaledBigDecimal(AMOUNT_ZERO_STRING);
    }
    /**
     * Method converts the given string decimal to big decimal with decimal point precisions is set to 2 places for the converted value
     *
     * @return converted decimal value in big decimal with scaled precision
     * */
    static BigDecimal get2ScaledBigDecimal(String amount) {
        return setBigDecimalScale(new BigDecimal(amount));
    }

    static BigDecimal setBigDecimalScale(BigDecimal bigDecimal) {
        return  bigDecimal.setScale(TWO_DECIMAL_SCALE, BigDecimal.ROUND_DOWN);
    }
    /**
     * Method to get the current system time in milliseconds
     *
     * @return current system time in milli seconds
     * */
    static long getCurrentTimeInUTC() {
        return Instant.now().toEpochMilli();
    }

    /**
     * Method to validate whether the given date is a future minute timestamp
     *
     * @return boolean indicating whether the given date is a future minute timestamp
     * */
    static boolean isFutureDate(String dateString) {
        long now = getCurrentTimeInUTC();
        return now < Instant.parse(dateString).toEpochMilli();
    }

    /**
     * Method to validate whether the given date string is a past minute timestamp
     *
     * @return boolean indicating whether the given date is a past minute timestamp
     * */
    static boolean isPastMinuteTime(String dateString) {
        long difference = getTimeDifferenceFromNow(dateString);
        return difference >= 60;
    }

    /**
     * Method to validate whether the given timestamp second is a past minute timestamp
     *
     * @return boolean indicating whether the given timestamp second is a past minute timestamp
     * */
    static boolean isPastMinuteTimeStamp(Long timestamp) {
        long now = Instant.now().getEpochSecond();
        return timestamp < now-60;
    }

    /**
     * Method to validate whether the given timestamp second is a past timestamp existing before current second
     *
     * @return boolean indicating whether the given timestamp second is a past timestamp before this very second
     * */
    static boolean isPastSecondTimeStamp(Long timestamp) {
        long now = Instant.now().getEpochSecond();
        return timestamp < now;
    }

    /**
     * Method to get the time difference between the given string timestamp against the current timestamp in seconds
     *
     * @return Long value indicating the time difference in seconds
     * */
    static long getTimeDifferenceFromNow(String dateString) {
        long now = getCurrentTimeInUTC() / ONE_SECOND_IN_MILLIS;
        long timeInSeconds = Instant.parse(dateString).getEpochSecond();
        return now - timeInSeconds;
    }

    /**
     * Method to validate the given data is valid to be persisted
     * Amount value - should not be empty and should be proper decimal value
     * Date String - should not be empty and should be in proper instant format
     *
     * @throws UnrecognizedDataFormatException if the given transaction data is invalid
     * */
    static void validateTransactionData(TransactionVO txn) {
        if(txn.getAmount().isEmpty() || !txn.getAmount().matches(VALID_DECIMAL_NUMBER_STRING_REGEX) || txn.getTimestamp().isEmpty()) throw new UnrecognizedDataFormatException();
        try{
            Instant.parse(txn.getTimestamp());
        }catch (Exception ex) {
            throw new UnrecognizedDataFormatException();
        }
    }

}
