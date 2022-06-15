package com.gnb.trades.Utils;

public class Transaction {

    private final String sku;
    private final double amount;
    private final String currency;

    public Transaction(String sku, double amount, String currency) {
        this.sku = sku;
        this.amount = amount;
        this.currency = currency;
    }

    public String getSku() {
        return sku;
    }

    public double getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }
}
