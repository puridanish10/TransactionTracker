package com.example.transactiontracker;

import java.util.Collections;
import java.util.List;

/**
 * Pattern analyzer for 'Bills' category transactions.
 * Detects unusually high bill amounts and rapid recurring bill payments within 24 hours.
 */
public class BillsPatternAnalyzer implements PatternAnalyzer {

    public static final double DEFAULT_HIGH_BILL_THRESHOLD = 6000.0;
    private static final long RECURRING_WINDOW_MS = 24 * 60 * 60 * 1000L; // 24 hours

    private final double highBillThreshold;

    public BillsPatternAnalyzer() {
        this(DEFAULT_HIGH_BILL_THRESHOLD);
    }

    public BillsPatternAnalyzer(double highBillThreshold) {
        this.highBillThreshold = highBillThreshold;
    }

    public BillsPatternAnalyzer(SettingsManager settings) {
        this(settings != null ? settings.getBillsThreshold() : DEFAULT_HIGH_BILL_THRESHOLD);
    }

    public double getHighBillThreshold() {
        return highBillThreshold;
    }

    @Override
    public String analyzePattern(Transaction tx, List<Transaction> categoryHistory) {
        List<Transaction> history = (categoryHistory != null) ? categoryHistory : Collections.emptyList();
        StringBuilder patternReasons = new StringBuilder();

        // 1. High bill amount
        if (tx.getAmount() >= highBillThreshold) {
            patternReasons.append("Unusually high bill amount (>₹").append((int) highBillThreshold).append("); ");
        }

        // 2. Multiple bill payments within 24 hours
        int recentCount = 0;
        for (Transaction prev : history) {
            if (prev.getId() != tx.getId()
                    && Math.abs(tx.getTimestamp() - prev.getTimestamp()) <= RECURRING_WINDOW_MS) {
                recentCount++;
            }
        }
        if (recentCount >= 1) {
            patternReasons.append("Multiple bill payments detected within 24 hours; ");
        }

        return patternReasons.length() > 0 ? patternReasons.toString().trim() : null;
    }
}
