package com.example.transactiontracker;

import java.util.Collections;
import java.util.List;

/**
 * Pattern analyzer for 'Food' category transactions.
 * Detects unusually high dining expenses and rapid succession food orders.
 */
public class FoodPatternAnalyzer implements PatternAnalyzer {

    public static final double DEFAULT_HIGH_FOOD_THRESHOLD = 2500.0;
    private static final long RAPID_INTERVAL_MS = 15 * 60 * 1000L; // 15 minutes

    private final double highFoodThreshold;

    public FoodPatternAnalyzer() {
        this(DEFAULT_HIGH_FOOD_THRESHOLD);
    }

    public FoodPatternAnalyzer(double highFoodThreshold) {
        this.highFoodThreshold = highFoodThreshold;
    }

    public FoodPatternAnalyzer(SettingsManager settings) {
        this(settings != null ? settings.getFoodThreshold() : DEFAULT_HIGH_FOOD_THRESHOLD);
    }

    public double getHighFoodThreshold() {
        return highFoodThreshold;
    }

    @Override
    public String analyzePattern(Transaction tx, List<Transaction> categoryHistory) {
        List<Transaction> history = (categoryHistory != null) ? categoryHistory : Collections.emptyList();
        StringBuilder patternReasons = new StringBuilder();

        // 1. High value dining transaction
        if (tx.getAmount() >= highFoodThreshold) {
            patternReasons.append("Unusual high dining expense (>₹").append((int) highFoodThreshold).append("); ");
        }

        // 2. Rapid food orders check (multiple orders within 15 mins)
        int recentCount = 0;
        for (Transaction prev : history) {
            if (prev.getId() != tx.getId()
                    && Math.abs(tx.getTimestamp() - prev.getTimestamp()) <= RAPID_INTERVAL_MS) {
                recentCount++;
            }
        }
        if (recentCount >= 1) {
            patternReasons.append("Rapid food orders detected (multiple orders in 15 min); ");
        }

        return patternReasons.length() > 0 ? patternReasons.toString().trim() : null;
    }
}
