package com.example.transactiontracker;

import java.util.Collections;
import java.util.List;

/**
 * Pattern analyzer for 'Travel' category transactions.
 * Detects high travel spend and rapid travel/transit charge velocity.
 */
public class TravelPatternAnalyzer implements PatternAnalyzer {

    public static final double DEFAULT_HIGH_TRAVEL_THRESHOLD = 7500.0;
    private static final long RAPID_INTERVAL_MS = 30 * 60 * 1000L; // 30 minutes

    private final double highTravelThreshold;

    public TravelPatternAnalyzer() {
        this(DEFAULT_HIGH_TRAVEL_THRESHOLD);
    }

    public TravelPatternAnalyzer(double highTravelThreshold) {
        this.highTravelThreshold = highTravelThreshold;
    }

    public TravelPatternAnalyzer(SettingsManager settings) {
        this(settings != null ? settings.getTravelThreshold() : DEFAULT_HIGH_TRAVEL_THRESHOLD);
    }

    public double getHighTravelThreshold() {
        return highTravelThreshold;
    }

    @Override
    public String analyzePattern(Transaction tx, List<Transaction> categoryHistory) {
        List<Transaction> history = (categoryHistory != null) ? categoryHistory : Collections.emptyList();
        StringBuilder patternReasons = new StringBuilder();

        // 1. High value travel expense check
        if (tx.getAmount() >= highTravelThreshold) {
            patternReasons.append("High travel expenditure (>₹").append((int) highTravelThreshold).append("); ");
        }

        // 2. High velocity travel charge check (multiple charges within 30 mins)
        int recentCount = 0;
        for (Transaction prev : history) {
            if (prev.getId() != tx.getId()
                    && Math.abs(tx.getTimestamp() - prev.getTimestamp()) <= RAPID_INTERVAL_MS) {
                recentCount++;
            }
        }
        if (recentCount >= 1) {
            patternReasons.append("Rapid travel charges detected (multiple charges in 30 min); ");
        }

        return patternReasons.length() > 0 ? patternReasons.toString().trim() : null;
    }
}
