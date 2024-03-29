package com.example.s1616573.coinz;


import com.google.firebase.Timestamp;

public class Coin {
    private String id;
    private double value;
    private String currency;
    private Timestamp dateCollected;
    private double goldValue;

    Coin(String id, double value, String currency) {
        this.id = id;
        this.value = value;
        this.currency = currency;
    }

    public Coin() { }

    public String getCurrency() {
        return currency;
    }

    public double getValue() {
        return value;
    }

    public String getId() {
        return id;
    }

    public void setDate(Timestamp date) {
        dateCollected = date;
    }

    public Timestamp getDateCollected() {
        return dateCollected;
    }

    public double getGoldValue() {
        return goldValue;
    }

    public void setGoldValue(double goldValue) {
        this.goldValue = goldValue;
    }
}
