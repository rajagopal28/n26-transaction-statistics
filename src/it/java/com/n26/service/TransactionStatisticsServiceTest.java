package com.n26.service;

import com.n26.exception.TransactionExpiredException;
import com.n26.model.Statistics;
import com.n26.model.TransactionVO;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.LongStream;

@RunWith(MockitoJUnitRunner.class)
public class TransactionStatisticsServiceTest extends TestCase {

    @InjectMocks
    TransactionStatisticsService transactionStatisticsService;
    @Test
    public void shouldAddTransactionToEntireMinute_fromGivenTransactionTime() {
        Map<Long, Statistics> mockMap = Mockito.mock(Map.class);
        ReadWriteLock mockLock = Mockito.mock(ReadWriteLock.class);
        Lock mockReadLock = Mockito.mock(Lock.class);
        Lock mockWriteLock = Mockito.mock(Lock.class);
        // Mockito.when(mockLock.readLock()).thenReturn(mockReadLock);
        Mockito.when(mockLock.writeLock()).thenReturn(mockWriteLock);
        Mockito.when(mockMap.getOrDefault(Mockito.anyLong(), Mockito.any(Statistics.class))).thenReturn(Mockito.mock(Statistics.class));
        ReflectionTestUtils.setField(transactionStatisticsService, "statisticsConcurrentHashMap", mockMap);
        ReflectionTestUtils.setField(transactionStatisticsService, "readWriteLock", mockLock);
        Long now = System.currentTimeMillis() - 10000; // Now-10s
        String s = Instant.ofEpochMilli(now).toString();
        TransactionVO transactionVO = new TransactionVO();
        transactionVO.setTimestamp(s);
        transactionVO.setAmount("10.12");
        transactionStatisticsService.addTransaction(transactionVO);
        Mockito.verify(mockMap, Mockito.times(60)).putIfAbsent(Mockito.anyLong(), Mockito.any(Statistics.class));
        Mockito.verify(mockLock, Mockito.times(2)).writeLock();
        Mockito.verify(mockWriteLock).lock();
        Mockito.verify(mockWriteLock).unlock();
    }

    @Test
    public void shouldAddTransactionToEntireMinute_fromGivenTransactionTime_FunctionalComputation() {
        Map<Long, Statistics> mockMap = new HashMap<>();
        ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
        ReflectionTestUtils.setField(transactionStatisticsService, "statisticsConcurrentHashMap", mockMap);
        ReflectionTestUtils.setField(transactionStatisticsService, "readWriteLock", readWriteLock);
        Long now = System.currentTimeMillis() - 8000; // Now-8s
        Instant currentInstant = Instant.ofEpochMilli(now);
        String s = currentInstant.toString();
        TransactionVO transactionVO = new TransactionVO();
        transactionVO.setTimestamp(s);
        transactionVO.setAmount("10.12");
        transactionStatisticsService.addTransaction(transactionVO);
        LongStream.range(currentInstant.getEpochSecond(), currentInstant.getEpochSecond()+60).allMatch(mockMap::containsKey);
        mockMap.entrySet().stream().allMatch(v -> v.getValue().getSum().equals(transactionVO.getDecimalAmount()));
        mockMap.entrySet().stream().allMatch(v -> v.getValue().getMax().equals(transactionVO.getDecimalAmount()));
        mockMap.entrySet().stream().allMatch(v -> v.getValue().getMin().equals(transactionVO.getDecimalAmount()));
        mockMap.entrySet().stream().allMatch(v -> v.getValue().getAvg().equals(transactionVO.getDecimalAmount()));
    }

    @Test
    public void testExpiredTransactionException() {
        Map<Long, Statistics> mockMap = new HashMap<>();
        ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
        ReflectionTestUtils.setField(transactionStatisticsService, "statisticsConcurrentHashMap", mockMap);
        ReflectionTestUtils.setField(transactionStatisticsService, "readWriteLock", readWriteLock);
        Long now = System.currentTimeMillis() - 70000; // Now-70s

        Instant currentInstant = Instant.ofEpochMilli(now);
        String s = currentInstant.toString();
        TransactionVO transactionVO = new TransactionVO();
        transactionVO.setTimestamp(s);
        transactionVO.setAmount("12.99");
       try{
           transactionStatisticsService.addTransaction(transactionVO);
           Assert.fail("Should not come here!");
       }catch (Exception ex) {
           Assert.assertTrue(ex instanceof TransactionExpiredException);
       }

        now = System.currentTimeMillis() - 60000; // Now-60s
        currentInstant = Instant.ofEpochMilli(now);
        s = currentInstant.toString();
        transactionVO.setTimestamp(s);
        transactionVO.setAmount("13.99");
        try{
            transactionStatisticsService.addTransaction(transactionVO);
            Assert.fail("Should not come here!");
        }catch (Exception ex) {
            Assert.assertTrue(ex instanceof TransactionExpiredException);
        }

    }
}