package com.example.s1616573.coinz;

public class Message {

    private String sender;
    private double gold;

    Message(String sender, double gold) {
        this.sender = sender;
        this.gold = gold;
    }

    public String getSender() {
        return sender;
    }

    public double getGold() {
        return gold;
    }
}
