package com.example.transactiontracker;

import java.util.List;
public class FraudDetectionEngine {

    private static final double HIGH_VALUE_THRESHOLD   = 5000.0;
    private static final long   DUPLICATE_WINDOW_MS    = 60_000L;
    private static final double SPIKE_MULTIPLIER       = 3.0;
    private static final int    MIN_HISTORY_FOR_SPIKE  = 3;

    private final DatabaseHelper db;

    public FraudDetectionEngine(DatabaseHelper db) {
        this.db = db;
    }

    /**
     * Runs all detection rules against a transaction.
     * Mutates tx.flagged and tx.flagReason before it is persisted.
     */
    public void analyze(Transaction tx) {
        StringBuilder reasons = new StringBuilder();

        if (isHighValue(tx)) {
            reasons.append("High value (>₹").append((int) HIGH_VALUE_THRESHOLD).append("); ");
        }
        if (isDuplicate(tx)) {
            reasons.append("Duplicate within 60s; ");
        }
        if (isCategorySpike(tx)) {
            reasons.append("Category spike (3× avg); ");
        }

        if (reasons.length() > 0) {
            tx.setFlagged(true);
            tx.setFlagReason(reasons.toString().trim());
        } else {
            tx.setFlagged(false);
            tx.setFlagReason("");
        }
    }

    // ── Rule implementations ─────────────────────────────────────────────────

    private boolean isHighValue(Transaction tx) {
        return tx.getAmount() >= HIGH_VALUE_THRESHOLD;
    }

    private boolean isDuplicate(Transaction tx) {
        List<Transaction> recent = db.getAllTransactions();
        for (Transaction existing : recent) {
            if (existing.getCategory().equals(tx.getCategory())
                    && Math.abs(existing.getAmount() - tx.getAmount()) < 0.01
                    && Math.abs(existing.getTimestamp() - tx.getTimestamp()) <= DUPLICATE_WINDOW_MS) {
                return true;
            }
        }
        return false;
    }

    private boolean isCategorySpike(Transaction tx) {
        List<Transaction> catHistory = db.getByCategory(tx.getCategory());
        if (catHistory.size() < MIN_HISTORY_FOR_SPIKE) return false;
        double avg = db.getCategoryAverage(tx.getCategory());
        return avg > 0 && tx.getAmount() > (SPIKE_MULTIPLIER * avg);
    }
}
