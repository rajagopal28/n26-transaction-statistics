package com.n26.service;

import com.n26.model.Statistics;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
public class TransactionStatisticsService {
    private ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    Map<Long, Statistics> statisticsConcurrentHashMap = new ConcurrentHashMap<>();
    // map stores the stats of the past one minutes -- total space complexity 119
    // for a transaction with time T within current minute we will have 60 entries (current minute) + (T+59) entries
    // there will be overlaps but worst case space complexity will be 119
    // Overall worst case complexity in Big-O ==> O(C) ==> O(1)
    // where C=119 is a constant and doesn't change with count of transactions

}
