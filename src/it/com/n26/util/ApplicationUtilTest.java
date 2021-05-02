package com.n26.util;

import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;

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

}