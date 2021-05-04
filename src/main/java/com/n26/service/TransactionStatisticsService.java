package com.n26.service;

import com.n26.exception.TransactionExpiredException;
import com.n26.exception.UnrecognizedDataFormatException;
import com.n26.model.Statistics;
import com.n26.model.TransactionVO;
import com.n26.util.ApplicationUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 *
 * Transaction Statistics Service takes care of all the Data Manipulation calls related to adding transactions,
 * getting current minute statistics and deleting all transaction data
 *
 * @author Rajagopal
 *
 */
@Service
@Slf4j
public class TransactionStatisticsService {
    // Share-Exclusive lock application for Reads/Writes on the shared resource
    private ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    /*
     * map stores the stats of the past one minutes -- total space complexity 119
     * for a transaction with time T within current minute(Now-x) we will have Now+x entries (current minute) + (T+59) entries
     * there will be overlaps but worst case space complexity will be 119
     * Overall worst case complexity in Big-O ==> O(C) ==> O(1)
     * where C=61 is a constant and doesn't change with count of transactions
    */
    private Map<Long, Statistics> statisticsConcurrentHashMap = new ConcurrentHashMap<>();

    /**
     * Adds new transaction to create statistics <br/>
     *
     * <i> Validations employed are to check the validity of timestamp and amount fields
     * Exceptions thrown on validation failures
     * Write lock is acquired to compute all the valid statistics related to the given transaction.</i><br/>
     *
     * Overall worst case complexity in Big-O ==> O(C) ==> O(1)<br/>
     *
     * Does not return any value
     */
    public void addTransaction(TransactionVO transaction) {
        log.info("Adding new transaction");
        log.info("Validating incoming transaction request");
        ApplicationUtil.validateTransactionData(transaction);
        if(ApplicationUtil.isPastMinuteTime(transaction.getTimestamp())){
           throw new TransactionExpiredException();
        }
        if(ApplicationUtil.isFutureDate(transaction.getTimestamp())){
            throw new UnrecognizedDataFormatException();
        }
        readWriteLock.writeLock().lock();
        log.info("Issuing writeLock for new transaction!");
        Instant timestamp = Instant.parse(transaction.getTimestamp());
        long now = Instant.now().getEpochSecond();
        long timestampEpochSecond = timestamp.getEpochSecond();
        // only take in to account from current second even if the txn is valid for duration past that
        for(long ts = now; ts <timestampEpochSecond+60; ts++ ) {
            Statistics s = statisticsConcurrentHashMap.getOrDefault(ts, new Statistics());
            s.maxOf(transaction.getDecimalAmount());
            s.minOf(transaction.getDecimalAmount());
            s.add(transaction.getDecimalAmount());
            statisticsConcurrentHashMap.putIfAbsent(ts, s);
        }
        log.info("Releasing write lock after aggregating past minute stats");
        readWriteLock.writeLock().unlock();
        log.info("End of Adding new transaction");
    }

    /**
     * Scheduled method to clear the transaction data every 10Seconds <br/>
     *
     * <i> Find all the expired timestamps and remove them from the MapStore
     *  Write locks acquired until all the expired statistics are removed from Store</i><br/>
     *
     * Overall worst case complexity in Big-O ==> O(C) ==> O(1)<br/>
     *  Does not return any value
     */
    @Scheduled(fixedDelay = ApplicationUtil.TEN_SECOND_IN_MILLIS)
    public void cleanupPastTransactions() {
        log.info("Trying to cleanup statistics that are not in current minute :: " + Instant.now().toString());
        log.info("Acquiring writeLock for cleaning up past minute stats!");
        readWriteLock.writeLock().lock();
        log.info("Current size of transactionsMap before cleanup :: "+statisticsConcurrentHashMap.size());
        Set<Long> keysToRemove = statisticsConcurrentHashMap.keySet().stream().filter(ApplicationUtil::isPastMinuteTimeStamp).collect(Collectors.toSet());
        keysToRemove.forEach(statisticsConcurrentHashMap::remove);
        keysToRemove.clear();
        log.info("Current size of transactionsMap after cleanup :: "+statisticsConcurrentHashMap.size());
        log.info("Releasing writeLock after cleaning up past minute stats!");
        readWriteLock.writeLock().unlock();
        log.info("Cleanup completed for statistics that are not in current minute");
    }

    /**
     * Get current minute Statistics <br/>
     *
     * <i> Computed Statistics for the current minute is fetched from the Map. <br/>
     * If no computed statistic is present, we will get the blank statistic with all Zeros
     * Read lock acquired until the statistic is fetched </i><br/>
     * Overall worst case complexity in Big-O ==> O(1).
     * Given that ConcurrentHashMap.get has O(1) time to fetch based in the Key<br/>
     *
     * @return Statistics
     */
    public Statistics getStatistics() {
        log.info("Trying to get statistics of past minute");
        readWriteLock.readLock().lock();
        log.info("Issuing readLock for getting current minute stats!");
        Long currentMinuteTimeStamp = Instant.now().getEpochSecond();
        Statistics currentMinuteStat = statisticsConcurrentHashMap.getOrDefault(currentMinuteTimeStamp, new Statistics());
        log.info("Releasing readLock after getting current minute stats!");
        readWriteLock.readLock().unlock();
        log.info("End of fetching current minute stat!");
        return currentMinuteStat;
    }

    /**
     * Removed all statistics data <br/>
     *
     * <i> Validations employed are to check the validity of timestamp and amount fields
     * Exceptions thrown on validation failures.
     * Write lock is held until the data is cleared.</i><br/>
     *
     * Overall worst case complexity in Big-O ==> O(1)<br/>
     *  Does not return any value
     */
    public void clearAllStatistics() {
        log.info("Trying to clear all existing statistics");
        readWriteLock.writeLock().lock();
        log.info("Issuing writeLock for clearing all stats!");
        statisticsConcurrentHashMap.clear();
        log.info("Releasing writeLock after clearing stats!");
        readWriteLock.writeLock().unlock();
        log.info("End of clearing all statistics in system!");
    }
}
