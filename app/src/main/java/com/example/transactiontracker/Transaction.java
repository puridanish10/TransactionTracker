package com.example.transactiontracker;

public class Transaction {
    private long   id;
    private String name;
    private double amount;
    private String category;
    private long   timestamp;
    private boolean flagged;
    private String  flagReason;

    public Transaction() {}

    public Transaction(String name, double amount, String category) {
        this.name      = name;
        this.amount    = amount;
        this.category  = category;
        this.timestamp = System.currentTimeMillis();
        this.flagged   = false;
        this.flagReason= "";
    }

    public long    getId()          { return id; }
    public String  getName()        { return name; }
    public double  getAmount()      { return amount; }
    public String  getCategory()    { return category; }
    public long    getTimestamp()   { return timestamp; }
    public boolean isFlagged()      { return flagged; }
    public String  getFlagReason()  { return flagReason; }

    public void setId(long id)                  { this.id = id; }
    public void setName(String name)            { this.name = name; }
    public void setAmount(double amount)        { this.amount = amount; }
    public void setCategory(String category)    { this.category = category; }
    public void setTimestamp(long timestamp)    { this.timestamp = timestamp; }
    public void setFlagged(boolean flagged)     { this.flagged = flagged; }
    public void setFlagReason(String reason)    { this.flagReason = reason; }
}
