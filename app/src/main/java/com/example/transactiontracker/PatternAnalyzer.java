package com.example.transactiontracker;

import java.util.List;

/**
 * Interface for analyzing pattern anomalies specific to transaction types/categories.
 */
public interface PatternAnalyzer {
    /**
     * Analyzes a transaction against historical transactions of the same category.
     *
     * @param tx The current transaction being analyzed.
     * @param categoryHistory Historical list of transactions in this category.
     * @return Description of any detected pattern anomaly, or null if normal.
     */
    String analyzePattern(Transaction tx, List<Transaction> categoryHistory);
}
