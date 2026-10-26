package com.example.transactiontracker;

import java.util.Collections;
import java.util.List;

/**
 * Default pattern analyzer for uncategorized or general transactions.
 */
public class DefaultPatternAnalyzer implements PatternAnalyzer {

    public static final double DEFAULT_HIGH_GENERAL_THRESHOLD = 3000.0;
    private static final long RAPID_WINDOW_MS = 10 * 60 * 1000L; // 10 minutes

    private final double highGeneralThreshold;

    public DefaultPatternAnalyzer() {
        this(DEFAULT_HIGH_GENERAL_THRESHOLD);
    }

    public DefaultPatternAnalyzer(double highGeneralThreshold) {
        this.highGeneralThreshold = highGeneralThreshold;
    }

    public DefaultPatternAnalyzer(SettingsManager settings) {
        this(settings != null ? settings.getGeneralThreshold() : DEFAULT_HIGH_GENERAL_THRESHOLD);
    }

    public double getHighGeneralThreshold() {
        return highGeneralThreshold;
    }

    @Override
    public String analyzePattern(Transaction tx, List<Transaction> categoryHistory) {
        List<Transaction> history = (categoryHistory != null) ? categoryHistory : Collections.emptyList();
        StringBuilder patternReasons = new StringBuilder();

        if (tx.getAmount() >= highGeneralThreshold) {
            patternReasons.append("Unusual expense for category (>₹").append((int) highGeneralThreshold).append("); ");
        }

        int recentCount = 0;
        for (Transaction prev : history) {
            if (prev.getId() != tx.getId()
                    && Math.abs(tx.getTimestamp() - prev.getTimestamp()) <= RAPID_WINDOW_MS) {
                recentCount++;
            }
        }
        if (recentCount >= 2) {
            patternReasons.append("High transaction frequency in short window; ");
        }

        return patternReasons.length() > 0 ? patternReasons.toString().trim() : null;
    }
}
