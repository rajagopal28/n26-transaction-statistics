package com.n26.controller;

import com.n26.model.Statistics;
import com.n26.model.TransactionVO;
import com.n26.service.TransactionStatisticsService;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RunWith(MockitoJUnitRunner.class)
public class StatisticsControllerTest extends TestCase {

    @Mock
    TransactionStatisticsService transactionStatisticsService;
    @InjectMocks
    StatisticsController statisticsController;

    @Test
    public void testControllerInvokeGetStatisticsCall() {
        Statistics expected = Mockito.mock(Statistics.class);
        Mockito.when(transactionStatisticsService.getStatistics()).thenReturn(expected);
        ResponseEntity<Statistics> statistics = statisticsController.statistics();
        Mockito.verify(transactionStatisticsService).getStatistics();
        Assert.assertEquals(expected, statistics.getBody());
        Assert.assertEquals(HttpStatus.OK, statistics.getStatusCode());
    }

    @Test
    public void testControllerInvokeDeleteStatisticsCall() {
        ResponseEntity<Void> voidResponseEntity = statisticsController.deleteTransactions();
        Mockito.verify(transactionStatisticsService).clearAllStatistics();
        Assert.assertEquals(HttpStatus.NO_CONTENT, voidResponseEntity.getStatusCode());
    }


    @Test
    public void testControllerInvokeAddTransactionsCall() {
        TransactionVO transaction = Mockito.mock(TransactionVO.class);
        ResponseEntity<Void> voidResponseEntity = statisticsController.addTransactions(transaction);
        Mockito.verify(transactionStatisticsService).addTransaction(transaction);
        Assert.assertEquals(HttpStatus.CREATED, voidResponseEntity.getStatusCode());
    }
}