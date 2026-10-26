package com.example.transactiontracker;

import java.util.Locale;

/**
 * Factory class that returns the appropriate {@link PatternAnalyzer}
 * for a given transaction category / type.
 */
public class PatternAnalyzerFactory {

    public static PatternAnalyzer getAnalyzer(String category) {
        SettingsManager nullSettings = null;
        return getAnalyzer(category, nullSettings);
    }

    public static PatternAnalyzer getAnalyzer(String category, SettingsManager settings) {
        if (settings != null) {
            double threshold = settings.getCategoryThreshold(category);
            return getAnalyzer(category, threshold);
        }

        if (category == null) {
            return new DefaultPatternAnalyzer();
        }

        switch (category.trim().toLowerCase(Locale.ROOT)) {
            case "food":
                return new FoodPatternAnalyzer();
            case "travel":
                return new TravelPatternAnalyzer();
            case "shopping":
                return new ShoppingPatternAnalyzer();
            case "bills":
                return new BillsPatternAnalyzer();
            default:
                return new DefaultPatternAnalyzer();
        }
    }

    public static PatternAnalyzer getAnalyzer(String category, double customThreshold) {
        if (category == null) {
            return new DefaultPatternAnalyzer(customThreshold);
        }

        switch (category.trim().toLowerCase(Locale.ROOT)) {
            case "food":
                return new FoodPatternAnalyzer(customThreshold);
            case "travel":
                return new TravelPatternAnalyzer(customThreshold);
            case "shopping":
                return new ShoppingPatternAnalyzer(customThreshold);
            case "bills":
                return new BillsPatternAnalyzer(customThreshold);
            default:
                return new DefaultPatternAnalyzer(customThreshold);
        }
    }
}
