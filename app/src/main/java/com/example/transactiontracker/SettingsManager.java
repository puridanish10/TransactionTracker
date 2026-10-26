package com.example.transactiontracker;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Manages user settings and configurable anomaly detection parameters.
 * Uses SharedPreferences for persistence with fallback defaults.
 */
public class SettingsManager {

    private static final String PREF_NAME = "anomaly_settings";

    public static final String KEY_GLOBAL_THRESHOLD   = "key_global_threshold";
    public static final String KEY_TRAVEL_THRESHOLD   = "key_travel_threshold";
    public static final String KEY_FOOD_THRESHOLD     = "key_food_threshold";
    public static final String KEY_SHOPPING_THRESHOLD = "key_shopping_threshold";
    public static final String KEY_BILLS_THRESHOLD    = "key_bills_threshold";
    public static final String KEY_GENERAL_THRESHOLD  = "key_general_threshold";

    public static final double DEFAULT_GLOBAL_THRESHOLD   = 5000.0;
    public static final double DEFAULT_TRAVEL_THRESHOLD   = 7500.0;
    public static final double DEFAULT_FOOD_THRESHOLD     = 2500.0;
    public static final double DEFAULT_SHOPPING_THRESHOLD = 4000.0;
    public static final double DEFAULT_BILLS_THRESHOLD    = 6000.0;
    public static final double DEFAULT_GENERAL_THRESHOLD  = 3000.0;

    private final SharedPreferences prefs;
    private final Map<String, Double> inMemoryOverrides = new HashMap<>();

    public SettingsManager(Context context) {
        if (context != null) {
            this.prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        } else {
            this.prefs = null;
        }
    }

    public double getGlobalHighValueThreshold() {
        Double val = inMemoryOverrides.get(KEY_GLOBAL_THRESHOLD);
        if (val != null) {
            return val;
        }
        if (prefs != null) {
            return Double.longBitsToDouble(prefs.getLong(KEY_GLOBAL_THRESHOLD, Double.doubleToLongBits(DEFAULT_GLOBAL_THRESHOLD)));
        }
        return DEFAULT_GLOBAL_THRESHOLD;
    }

    public void setGlobalHighValueThreshold(double threshold) {
        inMemoryOverrides.put(KEY_GLOBAL_THRESHOLD, threshold);
        if (prefs != null) {
            prefs.edit().putLong(KEY_GLOBAL_THRESHOLD, Double.doubleToLongBits(threshold)).apply();
        }
    }

    public double getTravelThreshold() {
        Double val = inMemoryOverrides.get(KEY_TRAVEL_THRESHOLD);
        if (val != null) {
            return val;
        }
        if (prefs != null) {
            return Double.longBitsToDouble(prefs.getLong(KEY_TRAVEL_THRESHOLD, Double.doubleToLongBits(DEFAULT_TRAVEL_THRESHOLD)));
        }
        return DEFAULT_TRAVEL_THRESHOLD;
    }

    public void setTravelThreshold(double threshold) {
        inMemoryOverrides.put(KEY_TRAVEL_THRESHOLD, threshold);
        if (prefs != null) {
            prefs.edit().putLong(KEY_TRAVEL_THRESHOLD, Double.doubleToLongBits(threshold)).apply();
        }
    }

    public double getFoodThreshold() {
        Double val = inMemoryOverrides.get(KEY_FOOD_THRESHOLD);
        if (val != null) {
            return val;
        }
        if (prefs != null) {
            return Double.longBitsToDouble(prefs.getLong(KEY_FOOD_THRESHOLD, Double.doubleToLongBits(DEFAULT_FOOD_THRESHOLD)));
        }
        return DEFAULT_FOOD_THRESHOLD;
    }

    public void setFoodThreshold(double threshold) {
        inMemoryOverrides.put(KEY_FOOD_THRESHOLD, threshold);
        if (prefs != null) {
            prefs.edit().putLong(KEY_FOOD_THRESHOLD, Double.doubleToLongBits(threshold)).apply();
        }
    }

    public double getShoppingThreshold() {
        Double val = inMemoryOverrides.get(KEY_SHOPPING_THRESHOLD);
        if (val != null) {
            return val;
        }
        if (prefs != null) {
            return Double.longBitsToDouble(prefs.getLong(KEY_SHOPPING_THRESHOLD, Double.doubleToLongBits(DEFAULT_SHOPPING_THRESHOLD)));
        }
        return DEFAULT_SHOPPING_THRESHOLD;
    }

    public void setShoppingThreshold(double threshold) {
        inMemoryOverrides.put(KEY_SHOPPING_THRESHOLD, threshold);
        if (prefs != null) {
            prefs.edit().putLong(KEY_SHOPPING_THRESHOLD, Double.doubleToLongBits(threshold)).apply();
        }
    }

    public double getBillsThreshold() {
        Double val = inMemoryOverrides.get(KEY_BILLS_THRESHOLD);
        if (val != null) {
            return val;
        }
        if (prefs != null) {
            return Double.longBitsToDouble(prefs.getLong(KEY_BILLS_THRESHOLD, Double.doubleToLongBits(DEFAULT_BILLS_THRESHOLD)));
        }
        return DEFAULT_BILLS_THRESHOLD;
    }

    public void setBillsThreshold(double threshold) {
        inMemoryOverrides.put(KEY_BILLS_THRESHOLD, threshold);
        if (prefs != null) {
            prefs.edit().putLong(KEY_BILLS_THRESHOLD, Double.doubleToLongBits(threshold)).apply();
        }
    }

    public double getGeneralThreshold() {
        Double val = inMemoryOverrides.get(KEY_GENERAL_THRESHOLD);
        if (val != null) {
            return val;
        }
        if (prefs != null) {
            return Double.longBitsToDouble(prefs.getLong(KEY_GENERAL_THRESHOLD, Double.doubleToLongBits(DEFAULT_GENERAL_THRESHOLD)));
        }
        return DEFAULT_GENERAL_THRESHOLD;
    }

    public void setGeneralThreshold(double threshold) {
        inMemoryOverrides.put(KEY_GENERAL_THRESHOLD, threshold);
        if (prefs != null) {
            prefs.edit().putLong(KEY_GENERAL_THRESHOLD, Double.doubleToLongBits(threshold)).apply();
        }
    }

    /**
     * Gets the custom threshold configured for a given category name.
     */
    public double getCategoryThreshold(String category) {
        if (category == null) return getGeneralThreshold();
        switch (category.trim().toLowerCase(Locale.ROOT)) {
            case "food":
                return getFoodThreshold();
            case "travel":
                return getTravelThreshold();
            case "shopping":
                return getShoppingThreshold();
            case "bills":
                return getBillsThreshold();
            default:
                return getGeneralThreshold();
        }
    }
}
