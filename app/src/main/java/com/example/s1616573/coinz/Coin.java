package com.example.s1616573.coinz;

import com.mapbox.mapboxsdk.annotations.Marker;

public class Coin {
    private String id;
    private double value;
    private String currency;

    public Coin(String id, double value, String currency) {
        this.id = id;
        this.value = value;
        this.currency = currency;
    }


    public String getCurrency() {
        return currency;
    }

    public double getValue() {
        return value;
    }

    public String getId() {
        return id;
    }
}
