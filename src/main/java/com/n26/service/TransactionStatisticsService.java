package com.n26.service;

import com.n26.exception.TransactionExpiredException;
import com.n26.model.Statistics;
import com.n26.model.TransactionVO;
import com.n26.util.ApplicationUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
@Slf4j
public class TransactionStatisticsService {
    private ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private Map<Long, Statistics> statisticsConcurrentHashMap = new ConcurrentHashMap<>();
    // map stores the stats of the past one minutes -- total space complexity 119
    // for a transaction with time T within current minute we will have 60 entries (current minute) + (T+59) entries
    // there will be overlaps but worst case space complexity will be 119
    // Overall worst case complexity in Big-O ==> O(C) ==> O(1)
    // where C=119 is a constant and doesn't change with count of transactions

    public void addTransaction(TransactionVO transaction) {
        log.info("Adding new transaction");
        if(ApplicationUtil.isPastMinuteTime(transaction.getTimestamp())){
           throw new TransactionExpiredException();
        }
        if(ApplicationUtil.isFutureDate(transaction.getTimestamp())){
            throw new TransactionExpiredException();
        }
        readWriteLock.writeLock().lock();
        log.info("Issuing writeLock for new transaction!");
        Instant timestamp = Instant.parse(transaction.getTimestamp());
        long timestampEpochSecond = timestamp.getEpochSecond();
        for(long ts = timestampEpochSecond; ts <timestampEpochSecond+60; ts++ ) {
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
}
