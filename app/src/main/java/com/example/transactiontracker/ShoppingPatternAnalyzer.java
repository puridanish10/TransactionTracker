package com.example.transactiontracker;

import java.util.Collections;
import java.util.List;

/**
 * Pattern analyzer for 'Shopping' category transactions.
 * Detects high retail expenses and shopping spree patterns.
 */
public class ShoppingPatternAnalyzer implements PatternAnalyzer {

    public static final double DEFAULT_HIGH_SHOPPING_THRESHOLD = 4000.0;
    private static final long SPREE_INTERVAL_MS = 60 * 60 * 1000L; // 1 hour

    private final double highShoppingThreshold;

    public ShoppingPatternAnalyzer() {
        this(DEFAULT_HIGH_SHOPPING_THRESHOLD);
    }

    public ShoppingPatternAnalyzer(double highShoppingThreshold) {
        this.highShoppingThreshold = highShoppingThreshold;
    }

    public ShoppingPatternAnalyzer(SettingsManager settings) {
        this(settings != null ? settings.getShoppingThreshold() : DEFAULT_HIGH_SHOPPING_THRESHOLD);
    }

    public double getHighShoppingThreshold() {
        return highShoppingThreshold;
    }

    @Override
    public String analyzePattern(Transaction tx, List<Transaction> categoryHistory) {
        List<Transaction> history = (categoryHistory != null) ? categoryHistory : Collections.emptyList();
        StringBuilder patternReasons = new StringBuilder();

        // 1. High value retail expense
        if (tx.getAmount() >= highShoppingThreshold) {
            patternReasons.append("High retail expense (>₹").append((int) highShoppingThreshold).append("); ");
        }

        // 2. Shopping spree pattern (3 or more purchases in 1 hour)
        int recentCount = 0;
        for (Transaction prev : history) {
            if (prev.getId() != tx.getId()
                    && Math.abs(tx.getTimestamp() - prev.getTimestamp()) <= SPREE_INTERVAL_MS) {
                recentCount++;
            }
        }
        if (recentCount >= 2) {
            patternReasons.append("Shopping spree pattern detected (3+ purchases in 1 hour); ");
        }

        return patternReasons.length() > 0 ? patternReasons.toString().trim() : null;
    }
}
