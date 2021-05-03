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
/**
 *
 * Statistics Controller takes care of all the API endpoint calls related to adding transactions,
 * getting current minute statistics and deleting all transaction data
 *
 * @author Rajagopal
 *
 */
@RestController
public class StatisticsController {
    @Autowired
    private TransactionStatisticsService transactionStatisticsService;

    /**
     * Endpoint handler to get current time period statistics based on the data persisted in the service
     *
     * @return ResponseEntity with Statistics content and OK status code to indicate the successful data fetch
     * */
    @RequestMapping(value = ApplicationUtil.APPLICATION_PATH_GET_STATISTICS, method = RequestMethod.GET)
    public ResponseEntity<Statistics> statistics() {
        Statistics statistics = transactionStatisticsService.getStatistics();
        return new ResponseEntity<>(statistics, HttpStatus.OK);
    }

    /**
     * Endpoint handler to delete all the transactions by clearing the data persisted in the service layer
     *
     * @return ResponseEntity with NO_CONTENT status code to indicate the data is cleared
     * */
    @RequestMapping(value = ApplicationUtil.APPLICATION_PATH_TRANSACTIONS, method = RequestMethod.DELETE)
    public ResponseEntity<Void> deleteTransactions() {
        transactionStatisticsService.clearAllStatistics();
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * Endpoint handler to add given transaction to the time period statistics and persist the data in the service
     *
     * @return ResponseEntity with CREATED status code to indicate the successful data population
     * */
    @RequestMapping(value = ApplicationUtil.APPLICATION_PATH_TRANSACTIONS, method = RequestMethod.POST)
    public ResponseEntity<Void> addTransactions(@RequestBody TransactionVO transaction) {
        transactionStatisticsService.addTransaction(transaction);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
