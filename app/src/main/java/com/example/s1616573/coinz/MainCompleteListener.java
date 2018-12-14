package com.example.s1616573.coinz;

import com.mapbox.mapboxsdk.annotations.Marker;

import java.util.ArrayList;

public interface MainCompleteListener {

    void downloadComplete(ArrayList<String> pickedUpCoins);

    void pickUpComplete(Marker m);
}
