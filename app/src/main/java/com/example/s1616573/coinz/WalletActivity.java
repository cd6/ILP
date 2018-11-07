package com.example.s1616573.coinz;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class WalletActivity extends AppCompatActivity implements RecyclerViewAdapter.ItemClickListener {

    private String tag = "WalletActivity";
    private UserFirestore userFirestore;
    private FirebaseAuth mAuth;
    private RecyclerView recyclerView;
    private RecyclerViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);
        recyclerView = findViewById(R.id.rv_coins);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void onStart() {
        super.onStart();

        mAuth = FirebaseAuth.getInstance();
        userFirestore = new UserFirestore(mAuth);
        userFirestore.getCoinsInWallet(this);
    }

    public void showCoins(List<Coin> coins) {
        List<String> coinIds = new ArrayList<>();
        for (Coin c: coins) {
            coinIds.add(c.getId());
        }
        adapter = new RecyclerViewAdapter(this, coinIds);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(View view, int position) {
        Toast.makeText(this, "You clicked " + adapter.getItem(position) + " on row number " + position, Toast.LENGTH_SHORT).show();
    }
}
