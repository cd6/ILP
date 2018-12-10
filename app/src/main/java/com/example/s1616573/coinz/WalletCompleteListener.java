package com.example.s1616573.coinz;

import java.util.List;

public interface WalletCompleteListener {
    void getCoinsComplete(List<Coin> result);

    void transactionSucceeded(Boolean result);

    void getNumberDepositedComplete(int result);

    void chooseUserComplete(String userTo);

    void getUsernameComplete(String result);
}
