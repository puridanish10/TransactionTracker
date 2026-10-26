package com.example.transactiontracker;

import java.util.List;

public class FraudDetectionEngine {

    private static final long   DUPLICATE_WINDOW_MS    = 60_000L;
    private static final double SPIKE_MULTIPLIER       = 3.0;
    private static final int    MIN_HISTORY_FOR_SPIKE  = 3;

    private final DatabaseHelper db;
    private final SettingsManager settings;

    public FraudDetectionEngine(DatabaseHelper db) {
        this(db, new SettingsManager(null));
    }

    public FraudDetectionEngine(DatabaseHelper db, SettingsManager settings) {
        this.db = db;
        this.settings = (settings != null) ? settings : new SettingsManager(null);
    }

    public SettingsManager getSettings() {
        return settings;
    }

    /**
     * Runs all detection rules, category pattern analysis, and quantitative risk scoring against a transaction.
     * Mutates tx.flagged, tx.flagReason, and tx.riskScore before it is persisted.
     */
    public void analyze(Transaction tx) {
        StringBuilder reasons = new StringBuilder();

        double highValueThreshold = settings.getGlobalHighValueThreshold();

        if (isHighValue(tx, highValueThreshold)) {
            reasons.append("High value (>₹").append((int) highValueThreshold).append("); ");
        }
        if (isDuplicate(tx)) {
            reasons.append("Duplicate within 60s; ");
        }
        if (isCategorySpike(tx)) {
            reasons.append("Category spike (3× avg); ");
        }

        // Run category-specific pattern analyzer
        List<Transaction> catHistory = (db != null) ? db.getByCategory(tx.getCategory()) : null;
        String patternReason = PatternAnalyzerFactory.getAnalyzer(tx.getCategory(), settings).analyzePattern(tx, catHistory);

        if (patternReason != null && !patternReason.isEmpty()) {
            reasons.append(patternReason).append("; ");
        }

        String rawReason = reasons.toString().trim();
        String finalReason = rawReason.endsWith(";")
                ? rawReason.substring(0, rawReason.length() - 1).trim()
                : rawReason;

        if (!finalReason.isEmpty()) {
            tx.setFlagged(true);
            tx.setFlagReason(finalReason);
        } else {
            tx.setFlagged(false);
            tx.setFlagReason("");
        }

        // Calculate dynamic quantitative Risk Score (0-100)
        List<Transaction> allTx = (db != null) ? db.getAllTransactions() : null;
        double catAvg = (db != null) ? db.getCategoryAverage(tx.getCategory()) : 0;
        int riskScore = RiskScoreCalculator.calculateRiskScore(tx, allTx, catHistory, catAvg, patternReason, highValueThreshold);
        tx.setRiskScore(riskScore);
    }

    // ── Rule implementations ─────────────────────────────────────────────────

    private boolean isHighValue(Transaction tx, double threshold) {
        return tx.getAmount() >= threshold;
    }

    private boolean isDuplicate(Transaction tx) {
        if (db == null) return false;
        List<Transaction> recent = db.getAllTransactions();
        for (Transaction existing : recent) {
            if (existing.getCategory() != null
                    && existing.getCategory().equalsIgnoreCase(tx.getCategory())
                    && Math.abs(existing.getAmount() - tx.getAmount()) < 0.01
                    && Math.abs(existing.getTimestamp() - tx.getTimestamp()) <= DUPLICATE_WINDOW_MS) {
                return true;
            }
        }
        return false;
    }

    private boolean isCategorySpike(Transaction tx) {
        if (db == null) return false;
        List<Transaction> catHistory = db.getByCategory(tx.getCategory());
        if (catHistory.size() < MIN_HISTORY_FOR_SPIKE) return false;
        double avg = db.getCategoryAverage(tx.getCategory());
        return avg > 0 && tx.getAmount() > (SPIKE_MULTIPLIER * avg);
    }
}
