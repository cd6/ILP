package com.example.s1616573.coinz;

import android.os.Bundle;
import android.os.TestLooperManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

public class BankActivity extends AppCompatActivity {

    private String tag = "BankActivity";
    private UserFirestore userFirestore;
    private FirebaseAuth mAuth;
    private TextView textGold;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bank);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        textGold = findViewById(R.id.text_gold);
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth = FirebaseAuth.getInstance();
        userFirestore = new UserFirestore(mAuth);
        userFirestore.getGoldInBank(this);
    }

    public void showGold(double gold) {
        textGold.setText(String.format("You have\n\n%s\n\ngold", gold));
    }
}
