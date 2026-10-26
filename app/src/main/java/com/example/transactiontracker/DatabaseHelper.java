package com.example.transactiontracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

/**
 * SQLite data pipeline with data governance and validation checks.
 * Achieves zero integrity errors by enforcing constraints at write time.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME    = "transactions.db";
    private static final int    DB_VERSION = 2;

    public static final String TABLE          = "transactions";
    public static final String COL_ID         = "id";
    public static final String COL_NAME       = "name";
    public static final String COL_AMOUNT     = "amount";
    public static final String COL_CATEGORY   = "category";
    public static final String COL_TIMESTAMP  = "timestamp";
    public static final String COL_IS_FLAGGED = "is_flagged";
    public static final String COL_FLAG_REASON= "flag_reason";
    public static final String COL_RISK_SCORE = "risk_score";

    private static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE + " (" +
                    COL_ID          + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_NAME        + " TEXT NOT NULL, "                     +
                    COL_AMOUNT      + " REAL NOT NULL CHECK(amount > 0), "   +
                    COL_CATEGORY    + " TEXT NOT NULL, "                     +
                    COL_TIMESTAMP   + " INTEGER NOT NULL, "                  +
                    COL_IS_FLAGGED  + " INTEGER DEFAULT 0, "                 +
                    COL_FLAG_REASON + " TEXT DEFAULT '', "                   +
                    COL_RISK_SCORE  + " INTEGER DEFAULT 0"                   +
                    ");";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
        db.execSQL("CREATE INDEX idx_category ON " + TABLE + "(" + COL_CATEGORY + ");");
        db.execSQL("CREATE INDEX idx_timestamp ON " + TABLE + "(" + COL_TIMESTAMP + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE + " ADD COLUMN " + COL_RISK_SCORE + " INTEGER DEFAULT 0;");
        }
    }

    // ── Data governance: validate before every write ──────────────────────────

    /**
     * Inserts a transaction after running data validation checks.
     * Returns the row ID, or -1 if validation fails.
     */
    public long insertTransaction(Transaction tx) {
        if (!validate(tx)) return -1;

        ContentValues cv = new ContentValues();
        cv.put(COL_NAME,        tx.getName());
        cv.put(COL_AMOUNT,      tx.getAmount());
        cv.put(COL_CATEGORY,    tx.getCategory());
        cv.put(COL_TIMESTAMP,   tx.getTimestamp());
        cv.put(COL_IS_FLAGGED,  tx.isFlagged() ? 1 : 0);
        cv.put(COL_FLAG_REASON, tx.getFlagReason());
        cv.put(COL_RISK_SCORE,  tx.getRiskScore());

        return getWritableDatabase().insert(TABLE, null, cv);
    }

    /** Data quality validation — mirrors enterprise data stewardship patterns */
    private boolean validate(Transaction tx) {
        if (tx.getName() == null || tx.getName().trim().isEmpty()) return false;
        if (tx.getAmount() <= 0)                                    return false;
        if (tx.getCategory() == null || tx.getCategory().isEmpty()) return false;
        if (tx.getTimestamp() <= 0)                                 return false;
        return true;
    }

    public List<Transaction> getAllTransactions() {
        List<Transaction> list = new ArrayList<>();
        Cursor c = getReadableDatabase().query(
                TABLE, null, null, null, null, null, COL_TIMESTAMP + " DESC");
        while (c.moveToNext()) list.add(fromCursor(c));
        c.close();
        return list;
    }

    public List<Transaction> getByCategory(String category) {
        List<Transaction> list = new ArrayList<>();
        Cursor c = getReadableDatabase().query(
                TABLE, null, COL_CATEGORY + "=?", new String[]{category},
                null, null, COL_TIMESTAMP + " DESC");
        while (c.moveToNext()) list.add(fromCursor(c));
        c.close();
        return list;
    }

    public List<Transaction> getFlagged() {
        List<Transaction> list = new ArrayList<>();
        Cursor c = getReadableDatabase().query(
                TABLE, null, COL_IS_FLAGGED + "=1", null,
                null, null, COL_TIMESTAMP + " DESC");
        while (c.moveToNext()) list.add(fromCursor(c));
        c.close();
        return list;
    }

    public double getCategoryAverage(String category) {
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT AVG(" + COL_AMOUNT + ") FROM " + TABLE +
                        " WHERE " + COL_CATEGORY + "=?", new String[]{category});
        double avg = 0;
        if (c.moveToFirst()) avg = c.getDouble(0);
        c.close();
        return avg;
    }

    public int getTotalCount() {
        Cursor c = getReadableDatabase().rawQuery("SELECT COUNT(*) FROM " + TABLE, null);
        int count = 0;
        if (c.moveToFirst()) count = c.getInt(0);
        c.close();
        return count;
    }

    public double getTotalSpend() {
        Cursor c = getReadableDatabase().rawQuery("SELECT SUM(" + COL_AMOUNT + ") FROM " + TABLE, null);
        double sum = 0;
        if (c.moveToFirst()) sum = c.getDouble(0);
        c.close();
        return sum;
    }

    public void deleteTransaction(long id) {
        getWritableDatabase().delete(TABLE, COL_ID + "=?", new String[]{String.valueOf(id)});
    }

    private Transaction fromCursor(Cursor c) {
        Transaction tx = new Transaction();
        tx.setId(c.getLong(c.getColumnIndexOrThrow(COL_ID)));
        tx.setName(c.getString(c.getColumnIndexOrThrow(COL_NAME)));
        tx.setAmount(c.getDouble(c.getColumnIndexOrThrow(COL_AMOUNT)));
        tx.setCategory(c.getString(c.getColumnIndexOrThrow(COL_CATEGORY)));
        tx.setTimestamp(c.getLong(c.getColumnIndexOrThrow(COL_TIMESTAMP)));
        tx.setFlagged(c.getInt(c.getColumnIndexOrThrow(COL_IS_FLAGGED)) == 1);
        tx.setFlagReason(c.getString(c.getColumnIndexOrThrow(COL_FLAG_REASON)));
        int riskIdx = c.getColumnIndex(COL_RISK_SCORE);
        if (riskIdx != -1) {
            tx.setRiskScore(c.getInt(riskIdx));
        }
        return tx;
    }
}
