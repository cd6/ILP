package com.example.s1616573.coinz;

import java.util.ArrayList;

public interface DownloadCompleteListener {
    void downloadComplete(String result);

    void downloadComplete(ArrayList<String> pickedUpCoins);
}
