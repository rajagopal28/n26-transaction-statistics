package com.n26.controller;

import com.n26.model.Statistics;
import com.n26.model.TransactionVO;
import com.n26.service.TransactionStatisticsService;
import com.n26.util.ApplicationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StatisticsController {
    @Autowired
    private TransactionStatisticsService transactionStatisticsService;

    @RequestMapping(value = ApplicationUtil.APPLICATION_PATH_GET_STATISTICS, method = RequestMethod.GET)
    public ResponseEntity<Statistics> statistics() {
        Statistics statistics = transactionStatisticsService.getStatistics();
        return new ResponseEntity<>(statistics, HttpStatus.OK);
    }

    @RequestMapping(value = ApplicationUtil.APPLICATION_PATH_TRANSACTIONS, method = RequestMethod.DELETE)
    public ResponseEntity<Void> deleteTransactions() {
        transactionStatisticsService.clearAllStatistics();
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = ApplicationUtil.APPLICATION_PATH_TRANSACTIONS, method = RequestMethod.POST)
    public ResponseEntity<Void> addTransactions(@RequestBody TransactionVO transaction) {
        transactionStatisticsService.addTransaction(transaction);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
