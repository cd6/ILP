package com.example.s1616573.coinz;

import java.util.List;

public interface BankCompleteListener {

    void getGoldComplete(double result);

    void realtimeUpdateComplete(List<Message> result);
}
