package com.example.transactiontracker;

import org.junit.Test;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class PatternAnalyzerTest {

    @Test
    public void testFoodPatternAnalyzer_highSpend() {
        PatternAnalyzer analyzer = PatternAnalyzerFactory.getAnalyzer("Food");
        assertTrue(analyzer instanceof FoodPatternAnalyzer);

        Transaction tx = new Transaction("Fancy Dinner", 3000.0, "food");
        String result = analyzer.analyzePattern(tx, new ArrayList<>());
        assertNotNull(result);
        assertTrue(result.contains("Unusual high dining expense"));
    }

    @Test
    public void testFoodPatternAnalyzer_rapidOrders() {
        PatternAnalyzer analyzer = PatternAnalyzerFactory.getAnalyzer("food");

        long now = System.currentTimeMillis();
        Transaction prevTx = new Transaction("Lunch", 200.0, "food");
        prevTx.setId(1);
        prevTx.setTimestamp(now - 5 * 60 * 1000L); // 5 mins ago

        List<Transaction> history = new ArrayList<>();
        history.add(prevTx);

        Transaction currentTx = new Transaction("Coffee", 150.0, "food");
        currentTx.setId(2);
        currentTx.setTimestamp(now);

        String result = analyzer.analyzePattern(currentTx, history);
        assertNotNull(result);
        assertTrue(result.contains("Rapid food orders detected"));
    }

    @Test
    public void testTravelPatternAnalyzer_highSpendAndRapidCharges() {
        PatternAnalyzer analyzer = PatternAnalyzerFactory.getAnalyzer("travel");
        assertTrue(analyzer instanceof TravelPatternAnalyzer);

        long now = System.currentTimeMillis();
        Transaction prevTx = new Transaction("Flight", 5000.0, "travel");
        prevTx.setId(1);
        prevTx.setTimestamp(now - 10 * 60 * 1000L); // 10 mins ago

        List<Transaction> history = new ArrayList<>();
        history.add(prevTx);

        Transaction currentTx = new Transaction("Hotel", 8000.0, "travel");
        currentTx.setId(2);
        currentTx.setTimestamp(now);

        String result = analyzer.analyzePattern(currentTx, history);
        assertNotNull(result);
        assertTrue(result.contains("High travel expenditure"));
        assertTrue(result.contains("Rapid travel charges detected"));
    }

    @Test
    public void testShoppingPatternAnalyzer_spree() {
        PatternAnalyzer analyzer = PatternAnalyzerFactory.getAnalyzer("shopping");
        assertTrue(analyzer instanceof ShoppingPatternAnalyzer);

        long now = System.currentTimeMillis();
        Transaction tx1 = new Transaction("Shirt", 500.0, "shopping");
        tx1.setId(1);
        tx1.setTimestamp(now - 30 * 60 * 1000L);

        Transaction tx2 = new Transaction("Pants", 800.0, "shopping");
        tx2.setId(2);
        tx2.setTimestamp(now - 15 * 60 * 1000L);

        List<Transaction> history = new ArrayList<>();
        history.add(tx1);
        history.add(tx2);

        Transaction tx3 = new Transaction("Shoes", 1200.0, "shopping");
        tx3.setId(3);
        tx3.setTimestamp(now);

        String result = analyzer.analyzePattern(tx3, history);
        assertNotNull(result);
        assertTrue(result.contains("Shopping spree pattern detected"));
    }

    @Test
    public void testBillsPatternAnalyzer_multipleBills24h() {
        PatternAnalyzer analyzer = PatternAnalyzerFactory.getAnalyzer("bills");
        assertTrue(analyzer instanceof BillsPatternAnalyzer);

        long now = System.currentTimeMillis();
        Transaction bill1 = new Transaction("Electricity", 1500.0, "bills");
        bill1.setId(1);
        bill1.setTimestamp(now - 2 * 3600 * 1000L);

        List<Transaction> history = new ArrayList<>();
        history.add(bill1);

        Transaction bill2 = new Transaction("Water", 500.0, "bills");
        bill2.setId(2);
        bill2.setTimestamp(now);

        String result = analyzer.analyzePattern(bill2, history);
        assertNotNull(result);
        assertTrue(result.contains("Multiple bill payments detected within 24 hours"));
    }

    @Test
    public void testDefaultPatternAnalyzer_unknownCategory() {
        PatternAnalyzer analyzer = PatternAnalyzerFactory.getAnalyzer("crypto");
        assertTrue(analyzer instanceof DefaultPatternAnalyzer);

        Transaction tx = new Transaction("Bitcoin", 5000.0, "crypto");
        String result = analyzer.analyzePattern(tx, new ArrayList<>());
        assertNotNull(result);
        assertTrue(result.contains("Unusual expense for category"));
    }

    @Test
    public void testCustomThreshold_FoodPatternAnalyzer() {
        FoodPatternAnalyzer customAnalyzer = new FoodPatternAnalyzer(1000.0);
        assertEquals(1000.0, customAnalyzer.getHighFoodThreshold(), 0.001);

        Transaction tx1 = new Transaction("Lunch", 1500.0, "food");
        String res1 = customAnalyzer.analyzePattern(tx1, new ArrayList<>());
        assertNotNull(res1);
        assertTrue(res1.contains("Unusual high dining expense (>₹1000)"));

        Transaction tx2 = new Transaction("Snack", 800.0, "food");
        String res2 = customAnalyzer.analyzePattern(tx2, new ArrayList<>());
        assertNull(res2);
    }

    @Test
    public void testCustomThreshold_TravelPatternAnalyzer() {
        TravelPatternAnalyzer customAnalyzer = new TravelPatternAnalyzer(3000.0);
        assertEquals(3000.0, customAnalyzer.getHighTravelThreshold(), 0.001);

        Transaction tx = new Transaction("Train", 4000.0, "travel");
        String res = customAnalyzer.analyzePattern(tx, new ArrayList<>());
        assertNotNull(res);
        assertTrue(res.contains("High travel expenditure (>₹3000)"));
    }

    @Test
    public void testCustomThreshold_ShoppingPatternAnalyzer() {
        ShoppingPatternAnalyzer customAnalyzer = new ShoppingPatternAnalyzer(2000.0);
        assertEquals(2000.0, customAnalyzer.getHighShoppingThreshold(), 0.001);

        Transaction tx = new Transaction("Jacket", 2500.0, "shopping");
        String res = customAnalyzer.analyzePattern(tx, new ArrayList<>());
        assertNotNull(res);
        assertTrue(res.contains("High retail expense (>₹2000)"));
    }

    @Test
    public void testCustomThreshold_BillsPatternAnalyzer() {
        BillsPatternAnalyzer customAnalyzer = new BillsPatternAnalyzer(1500.0);
        assertEquals(1500.0, customAnalyzer.getHighBillThreshold(), 0.001);

        Transaction tx = new Transaction("Internet", 2000.0, "bills");
        String res = customAnalyzer.analyzePattern(tx, new ArrayList<>());
        assertNotNull(res);
        assertTrue(res.contains("Unusually high bill amount (>₹1500)"));
    }

    @Test
    public void testCustomThreshold_DefaultPatternAnalyzer() {
        DefaultPatternAnalyzer customAnalyzer = new DefaultPatternAnalyzer(1000.0);
        assertEquals(1000.0, customAnalyzer.getHighGeneralThreshold(), 0.001);

        Transaction tx = new Transaction("Misc", 1200.0, "other");
        String res = customAnalyzer.analyzePattern(tx, new ArrayList<>());
        assertNotNull(res);
        assertTrue(res.contains("Unusual expense for category (>₹1000)"));
    }

    @Test
    public void testSettingsManager_dynamicThresholds() {
        SettingsManager settings = new SettingsManager(null);
        settings.setTravelThreshold(2000.0);
        assertEquals(2000.0, settings.getTravelThreshold(), 0.001);

        PatternAnalyzer analyzer = PatternAnalyzerFactory.getAnalyzer("travel", settings);
        assertTrue(analyzer instanceof TravelPatternAnalyzer);
        assertEquals(2000.0, ((TravelPatternAnalyzer) analyzer).getHighTravelThreshold(), 0.001);

        Transaction tx = new Transaction("Taxi", 2500.0, "travel");
        String res = analyzer.analyzePattern(tx, new ArrayList<>());
        assertNotNull(res);
        assertTrue(res.contains("High travel expenditure (>₹2000)"));
    }

    @Test
    public void testFraudDetectionEngine_customHighValueSetting() {
        SettingsManager settings = new SettingsManager(null);
        settings.setGlobalHighValueThreshold(1000.0);

        FraudDetectionEngine engine = new FraudDetectionEngine(null, settings);
        Transaction tx = new Transaction("Tablet", 1200.0, "other");

        engine.analyze(tx);

        assertTrue(tx.isFlagged());
        assertTrue(tx.getFlagReason().contains("High value (>₹1000)"));
    }
}
