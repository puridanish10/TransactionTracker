package com.example.transactiontracker;

import java.util.List;

/**
 * Quantitative Risk Engine calculating a 0–100 risk score based on statistical
 * variance, transaction velocity, category spikes, and category-specific pattern anomalies.
 */
public class RiskScoreCalculator {

    public static final double DEFAULT_HIGH_VALUE_THRESHOLD = 5000.0;
    private static final double SPIKE_MULTIPLIER = 3.0;
    private static final long DUPLICATE_WINDOW_MS = 60_000L;

    /**
     * Computes the numeric risk score (0 - 100) for a given transaction with default threshold.
     */
    public static int calculateRiskScore(Transaction tx, List<Transaction> allTransactions, List<Transaction> categoryHistory, double categoryAvg, String patternReason) {
        return calculateRiskScore(tx, allTransactions, categoryHistory, categoryAvg, patternReason, DEFAULT_HIGH_VALUE_THRESHOLD);
    }

    /**
     * Computes the numeric risk score (0 - 100) for a given transaction with custom high value threshold.
     */
    public static int calculateRiskScore(Transaction tx, List<Transaction> allTransactions, List<Transaction> categoryHistory, double categoryAvg, String patternReason, double highValueThreshold) {
        int score = 0;

        // 1. High Amount Signal (Up to 30 pts)
        if (tx.getAmount() > 0) {
            double effectiveThreshold = highValueThreshold > 0 ? highValueThreshold : DEFAULT_HIGH_VALUE_THRESHOLD;
            double ratio = tx.getAmount() / effectiveThreshold;
            int amountPts = (int) Math.min(30, ratio * 20);
            score += amountPts;
        }

        // 2. Velocity / Duplicate Signal (Up to 25 pts)
        if (allTransactions != null) {
            for (Transaction prev : allTransactions) {
                if (prev.getId() != tx.getId()
                        && prev.getCategory() != null
                        && prev.getCategory().equalsIgnoreCase(tx.getCategory())
                        && Math.abs(prev.getAmount() - tx.getAmount()) < 0.01
                        && Math.abs(tx.getTimestamp() - prev.getTimestamp()) <= DUPLICATE_WINDOW_MS) {
                    score += 25;
                    break;
                }
            }
        }

        // 3. Category Spike Signal (Up to 25 pts)
        if (categoryHistory != null && categoryHistory.size() >= 3 && categoryAvg > 0) {
            if (tx.getAmount() > (SPIKE_MULTIPLIER * categoryAvg)) {
                score += 25;
            }
        }

        // 4. Pattern Anomaly Signal (Up to 20 pts)
        if (patternReason != null && !patternReason.trim().isEmpty()) {
            score += 20;
        }

        return Math.min(100, Math.max(0, score));
    }
}
