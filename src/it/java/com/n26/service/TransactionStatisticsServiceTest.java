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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
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
        Long now = System.currentTimeMillis() - 10000; // Now-10s ==> txn valid for Now+50s
        String s = Instant.ofEpochMilli(now).toString();
        TransactionVO transactionVO = new TransactionVO();
        transactionVO.setTimestamp(s);
        transactionVO.setAmount("10.12");
        transactionStatisticsService.addTransaction(transactionVO);
        Mockito.verify(mockMap, Mockito.times(50)).putIfAbsent(Mockito.anyLong(), Mockito.any(Statistics.class));
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
    public void shouldAddTransactionToEntireMinute_fromGivenTransactionTime_FunctionalComputation2() throws Exception {
        Map<Long, Statistics> mockMap = new HashMap<>();
        ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
        ReflectionTestUtils.setField(transactionStatisticsService, "statisticsConcurrentHashMap", mockMap);
        ReflectionTestUtils.setField(transactionStatisticsService, "readWriteLock", readWriteLock);
        Long t1s = System.currentTimeMillis() - 59000; // Now-59s
        Instant t1i = Instant.ofEpochMilli(t1s);
        TransactionVO t1 = new TransactionVO();
        t1.setTimestamp(t1i.toString());
        t1.setAmount("10.12");
        transactionStatisticsService.addTransaction(t1);
        Long t2s = System.currentTimeMillis() - 30000; // Now-30s
        Instant t2i = Instant.ofEpochMilli(t2s);
        TransactionVO t2 = new TransactionVO();
        t2.setTimestamp(t2i.toString());
        t2.setAmount("11.12");
        transactionStatisticsService.addTransaction(t2);

        Long t3s = System.currentTimeMillis() - 1000; // Now-1s
        Instant t3i = Instant.ofEpochMilli(t3s);
        TransactionVO t3 = new TransactionVO();
        t3.setTimestamp(t3i.toString());
        t3.setAmount("12.12");
        transactionStatisticsService.addTransaction(t3);

        Long t4s = System.currentTimeMillis() - 58000; // Now-58s
        Instant t4i = Instant.ofEpochMilli(t4s);
        TransactionVO t4 = new TransactionVO();
        t4.setTimestamp(t4i.toString());
        t4.setAmount("13.12");
        transactionStatisticsService.addTransaction(t4);

        Thread.sleep(1000); // thread sleep to expire t1 hence we will get only [t2,t3,t4]

        Long now = Instant.now().getEpochSecond();
        Statistics statistics = mockMap.get(now);
        Assert.assertEquals(BigDecimal.valueOf(36.36), statistics.getSum());
        Assert.assertEquals(BigDecimal.valueOf(12.12), statistics.getAvg());
        Assert.assertEquals(BigDecimal.valueOf(11.12), statistics.getMin());
        Assert.assertEquals(BigDecimal.valueOf(13.12), statistics.getMax());
        Assert.assertEquals(BigInteger.valueOf(3), statistics.getCount());
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

    @Test
    public void shouldGetTransactionCurrentMinute_MockObservable() {
        Map<Long, Statistics> mockMap = Mockito.mock(Map.class);
        ReadWriteLock mockLock = Mockito.mock(ReadWriteLock.class);
        Lock mockReadLock = Mockito.mock(Lock.class);
        // Lock mockWriteLock = Mockito.mock(Lock.class);
        // Mockito.when(mockLock.writeLock()).thenReturn(mockWriteLock);
        Mockito.when(mockLock.readLock()).thenReturn(mockReadLock);
        Statistics mockStat = Mockito.mock(Statistics.class);
        Mockito.when(mockMap.getOrDefault(Mockito.anyLong(), Mockito.any(Statistics.class))).thenReturn(mockStat);
        ReflectionTestUtils.setField(transactionStatisticsService, "statisticsConcurrentHashMap", mockMap);
        ReflectionTestUtils.setField(transactionStatisticsService, "readWriteLock", mockLock);

        Statistics statistics = transactionStatisticsService.getStatistics();
        Assert.assertEquals(mockStat, statistics);
        Mockito.verify(mockMap).getOrDefault(Mockito.anyLong(), Mockito.any(Statistics.class));
        Mockito.verify(mockLock, Mockito.times(2)).readLock();
        Mockito.verify(mockReadLock).lock();
        Mockito.verify(mockReadLock).unlock();
    }

    @Test
    public void shouldGetTransactionCurrentMinute_IfPresentValues_FunctionalComputation() throws Exception {
        Map<Long, Statistics> mockMap = new HashMap<>();
        ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
        Statistics s1 = new Statistics();
        s1.setAvg(BigDecimal.valueOf(12.12));
        s1.setSum(BigDecimal.valueOf(33.33));
        mockMap.put(Instant.now().getEpochSecond(), s1);

        ReflectionTestUtils.setField(transactionStatisticsService, "statisticsConcurrentHashMap", mockMap);
        ReflectionTestUtils.setField(transactionStatisticsService, "readWriteLock", readWriteLock);
        Statistics actual1 = transactionStatisticsService.getStatistics();

        Assert.assertEquals(s1, actual1);
    }

    @Test
    public void shouldGetTransactionCurrentMinute_IfNotPresentValues_ShouldSendEmpty() throws Exception {
        Map<Long, Statistics> mockMap = new HashMap<>();
        ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

        ReflectionTestUtils.setField(transactionStatisticsService, "statisticsConcurrentHashMap", mockMap);
        ReflectionTestUtils.setField(transactionStatisticsService, "readWriteLock", readWriteLock);
        Statistics actual2 = transactionStatisticsService.getStatistics();

        Assert.assertEquals(BigDecimal.ZERO, actual2.getSum());
        Assert.assertEquals(BigInteger.ZERO, actual2.getCount());
    }


    @Test
    public void shouldCleanupStatisticsFromPastMinute_Functional() throws Exception {
        Map<Long, Statistics> mockMap = new HashMap<>();
        ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
        Long now = Instant.now().getEpochSecond();

        mockMap.put(now, new Statistics());
        mockMap.put(now-10, new Statistics());
        mockMap.put(now-60, new Statistics());
        mockMap.put(now-70, new Statistics());
        mockMap.put(now-80, new Statistics());

        ReflectionTestUtils.setField(transactionStatisticsService, "statisticsConcurrentHashMap", mockMap);
        ReflectionTestUtils.setField(transactionStatisticsService, "readWriteLock", readWriteLock);
        transactionStatisticsService.cleanupPastTransactions();

        Assert.assertEquals(3, mockMap.size());
    }

    @Test
    public void shouldCleanupStatisticsFromPastMinute_MockObservable() {
        Map<Long, Statistics> mockMap = Mockito.mock(Map.class);
        Mockito.when(mockMap.size()).thenReturn(5);
        Long now = Instant.now().getEpochSecond();
        Mockito.when(mockMap.keySet()).thenReturn(new HashSet<>(Arrays.asList(now, now+10, now-10, now-60, now-70, now-80)));

        ReadWriteLock mockLock = Mockito.mock(ReadWriteLock.class);
        Lock mockWriteLock = Mockito.mock(Lock.class);
        Mockito.when(mockLock.writeLock()).thenReturn(mockWriteLock);
        Statistics mockStat = Mockito.mock(Statistics.class);
        Mockito.when(mockMap.getOrDefault(Mockito.anyLong(), Mockito.any(Statistics.class))).thenReturn(mockStat);
        ReflectionTestUtils.setField(transactionStatisticsService, "statisticsConcurrentHashMap", mockMap);
        ReflectionTestUtils.setField(transactionStatisticsService, "readWriteLock", mockLock);

        transactionStatisticsService.cleanupPastTransactions();

        Mockito.verify(mockLock, Mockito.times(2)).writeLock();
        Mockito.verify(mockWriteLock).lock();
        Mockito.verify(mockWriteLock).unlock();
        Mockito.verify(mockMap).remove(now-70);
        Mockito.verify(mockMap).remove(now-80);
    }
}