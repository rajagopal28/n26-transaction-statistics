package com.n26.service;

import com.n26.model.Statistics;
import com.n26.model.TransactionVO;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.LongStream;

@RunWith(MockitoJUnitRunner.class)
public class TransactionStatisticsServiceTest extends TestCase {

    @InjectMocks
    TransactionStatisticsService transactionStatisticsService;
    @Test
    public void shouldAddTransactionToEntireMinute_fromGivenTransactionTime() {
        Map<Long, Statistics> mockMap = Mockito.mock(Map.class);
        Mockito.when(mockMap.getOrDefault(Mockito.anyLong(), Mockito.any(Statistics.class))).thenReturn(Mockito.mock(Statistics.class));
        ReflectionTestUtils.setField(transactionStatisticsService, "statisticsConcurrentHashMap", mockMap);
        Long now = System.currentTimeMillis() - 10000; // Now-10s
        String s = Instant.ofEpochMilli(now).toString();
        TransactionVO transactionVO = new TransactionVO();
        transactionVO.setTimestamp(s);
        transactionVO.setAmount("10.12");
        transactionStatisticsService.addTransaction(transactionVO);
        Mockito.verify(mockMap, Mockito.times(60)).putIfAbsent(Mockito.anyLong(), Mockito.any(Statistics.class));
    }

    @Test
    public void shouldAddTransactionToEntireMinute_fromGivenTransactionTime_FunctionalComputation() {
        Map<Long, Statistics> mockMap = new HashMap<>();
        ReflectionTestUtils.setField(transactionStatisticsService, "statisticsConcurrentHashMap", mockMap);
        Long now = System.currentTimeMillis() - 10000; // Now-10s
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
}