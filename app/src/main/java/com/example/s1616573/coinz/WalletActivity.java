package com.example.s1616573.coinz;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WalletActivity extends AppCompatActivity implements RecyclerViewAdapter.ItemClickListener {

    private String tag = "WalletActivity";
    private UserFirestore userFirestore;
    private FirebaseAuth mAuth;
    private RecyclerView recyclerView;
    private RecyclerViewAdapter adapter;
    private Button depositButton;
    private final String preferencesFile = "MyPrefsFile";
    private int noSelected = 0;
    private HashMap<String, Double> rates;
    private HashMap<Integer, Coin> selectedCoins;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);
        recyclerView = findViewById(R.id.rv_coins);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        depositButton = findViewById(R.id.deposit_button);
        depositButton.setEnabled(false);

        selectedCoins = new HashMap<>();

        depositButton.setOnClickListener(view -> {
            deposit();
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth = FirebaseAuth.getInstance();
        userFirestore = new UserFirestore(mAuth);
        userFirestore.getCoinsInWallet(this);

        // Restore preferences
        SharedPreferences settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE);
        String geoJsonCoins = settings.getString("coinMap", "");
        try {
            getRates(geoJsonCoins);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void showCoins(List<Coin> coins) {
        adapter = new RecyclerViewAdapter(this, coins);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(View view, int position) {
        // toggle selection
        view.setSelected(!view.isSelected());

        Coin clicked = adapter.getItem(position);
        if (selectedCoins.containsKey(position)) {
            selectedCoins.remove(position);
        } else {
            selectedCoins.put(position, clicked);
        }

        depositButton.setEnabled(!selectedCoins.isEmpty());
    }

    public void getRates(String geoJson) throws JSONException {
        JSONObject obj = new JSONObject(geoJson);
        rates = new HashMap<>();
        String[] currencies = new String[]{"SHIL","DOLR","QUID","PENY"};
        for (String c : currencies) {
            String rate = obj.getJSONObject("rates").getString(c);
            rates.put(c, Double.parseDouble(rate));
        }
    }

    public void deposit() {
        double gold = 0;
        for (Integer p : selectedCoins.keySet()) {
            Coin c = selectedCoins.get(p);
            gold += c.getValue()*rates.get(c.getCurrency());
        }
        userFirestore.depositGold(gold);
        adapter.removeItems(selectedCoins.keySet());
        selectedCoins.clear();
    }

}
