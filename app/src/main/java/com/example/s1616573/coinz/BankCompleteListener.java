package com.example.s1616573.coinz;

import java.util.HashMap;
import java.util.List;

public interface BankCompleteListener {

    void getGoldComplete(double result);

    void realtimeUpdateComplete(HashMap<String, Message> result);
}
