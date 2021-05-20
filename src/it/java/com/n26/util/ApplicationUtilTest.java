package com.n26.util;

import com.n26.exception.UnrecognizedDataFormatException;
import com.n26.model.TransactionVO;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.Instant;

public class ApplicationUtilTest extends TestCase {

    @Test
    public void testFutureDateBooleanMethod() {
        Assert.assertTrue(ApplicationUtil.isFutureDate(Instant.now().plusMillis(60000).toString()));
        Assert.assertFalse(ApplicationUtil.isFutureDate(Instant.now().minusMillis(60000).toString()));
        Assert.assertFalse(ApplicationUtil.isFutureDate(Instant.now().toString()));
    }


    @Test
    public void testPasteDateBooleanMethod() {
        Assert.assertFalse(ApplicationUtil.isPastMinuteTime(Instant.now().plusMillis(60000).toString()));
        Assert.assertTrue(ApplicationUtil.isPastMinuteTime(Instant.now().minusMillis(60000).toString()));
        Assert.assertFalse(ApplicationUtil.isPastMinuteTime(Instant.now().toString()));
    }


    @Test
    public void testGetDifferenceInMilliSeconds_FromGivenTimeToNow() {
        Assert.assertEquals( -5, ApplicationUtil.getTimeDifferenceFromNow(Instant.now().plusMillis(5000).toString()));
        Assert.assertEquals( 60, ApplicationUtil.getTimeDifferenceFromNow(Instant.now().minusMillis(60000).toString()));
        Assert.assertEquals( 0, ApplicationUtil.getTimeDifferenceFromNow(Instant.now().toString()));
    }

    @Test
    public void testGetPastMinuteTimeStamp_FromGivenTimeToNow() {
        Assert.assertFalse(ApplicationUtil.isPastMinuteTimeStamp(Instant.now().plusMillis(5000).getEpochSecond()));
        Assert.assertTrue(ApplicationUtil.isPastMinuteTimeStamp(Instant.now().minusMillis(61000).getEpochSecond()));
        Assert.assertFalse(ApplicationUtil.isPastMinuteTimeStamp(Instant.now().getEpochSecond()));
    }

    @Test
    public void testGetPastSecondTimeStamp_FromGivenTimeToNow() {
        Assert.assertFalse(ApplicationUtil.isPastSecondTimeStamp(Instant.now().plusMillis(5000).getEpochSecond()));
        Assert.assertTrue(ApplicationUtil.isPastSecondTimeStamp(Instant.now().minusMillis(61000).getEpochSecond()));
        Assert.assertFalse(ApplicationUtil.isPastSecondTimeStamp(Instant.now().getEpochSecond()));
    }

    @Test
    public void testIsValidTransactionData_FailureCases() {
        TransactionVO txn = new TransactionVO();
        try{
            txn.setAmount("srs");
            txn.setTimestamp(Instant.now().toString());
            ApplicationUtil.validateTransactionData(txn);
            Assert.fail("Should not come here");
        } catch (Exception ex) {
            Assert.assertTrue(ex instanceof UnrecognizedDataFormatException);
        }
        try{
            txn.setAmount("");
            txn.setTimestamp(Instant.now().toString());
            ApplicationUtil.validateTransactionData(txn);
            Assert.fail("Should not come here");
        } catch (Exception ex) {
            Assert.assertTrue(ex instanceof UnrecognizedDataFormatException);
        }
        try{
            txn.setAmount("12.12");
            txn.setTimestamp("");
            ApplicationUtil.validateTransactionData(txn);
            Assert.fail("Should not come here");
        } catch (Exception ex) {
            Assert.assertTrue(ex instanceof UnrecognizedDataFormatException);
        }
        try{
            txn.setAmount("12.12");
            txn.setTimestamp("23-12-2021");
            ApplicationUtil.validateTransactionData(txn);
            Assert.fail("Should not come here");
        } catch (Exception ex) {
            Assert.assertTrue(ex instanceof UnrecognizedDataFormatException);
        }
    }

    @Test
    public void testIsValidTransactionData_SuccessCases() {
        TransactionVO txn = new TransactionVO();
        try{
            txn.setAmount("12.343");
            txn.setTimestamp(Instant.now().toString());
            ApplicationUtil.validateTransactionData(txn);

            txn.setAmount("12.23");
            ApplicationUtil.validateTransactionData(txn);

            txn.setAmount("120");
            ApplicationUtil.validateTransactionData(txn);
        } catch (Exception ex) {
            Assert.fail("Should not come here");
        }
    }

    @Test
    public void testSet2ScaledBigDecimalMethods_SuccessCases() {
        Assert.assertEquals("0.00", ApplicationUtil.get2ScaledBigDecimal().toString());
        Assert.assertEquals("12.45", ApplicationUtil.get2ScaledBigDecimal("12.4532423").toString());
        Assert.assertEquals("12.22", ApplicationUtil.get2ScaledBigDecimal("12.22").toString());
        Assert.assertEquals("120.00", ApplicationUtil.get2ScaledBigDecimal("120").toString());
        Assert.assertEquals("49.69", ApplicationUtil.get2ScaledBigDecimal("49.694495").toString());
        Assert.assertEquals("49.69", ApplicationUtil.get2ScaledBigDecimal("99388.99").divide(BigDecimal.valueOf(2000L), BigDecimal.ROUND_HALF_UP).toString());
    }

}