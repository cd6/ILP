package com.example.s1616573.coinz;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class BankActivity extends AppCompatActivity implements BankCompleteListener{

    private String tag = "BankActivity";
    private BankFirestore bankFirestore;
    private FirebaseAuth mAuth;
    private TextView textGold;
    private RecyclerView bankView;
    private BankRecyclerViewAdapter adapter;
    private Button btnClear;

    private String preferencesFile = "";
    private String lastGoldValue = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bank);
        bankView = findViewById(R.id.rv_messages);
        bankView.setLayoutManager(new LinearLayoutManager(this));
        btnClear = findViewById(R.id.btn_clear_messages);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar aBar = getSupportActionBar();
        aBar.setDisplayHomeAsUpEnabled(true);

        textGold = findViewById(R.id.text_gold);

        btnClear.setOnClickListener(view -> {
            bankFirestore.clearMessages();
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth = FirebaseAuth.getInstance();
        bankFirestore = new BankFirestore(mAuth);
        bankFirestore.bankCompleteListener = this;
        bankFirestore.getGoldInBank();

        preferencesFile = mAuth.getUid();

        // Restore preferences
        SharedPreferences settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE);

        // use "" as the default value (this might be the first time the app is run)
        lastGoldValue = settings.getString("lastGoldValue", "");

        bankFirestore.realtimeUpdateListener();
    }

    @Override
    public void onStop() {
        super.onStop();
        // Save gold value in case can't connect to network on next start
        SharedPreferences settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("lastGoldValue", lastGoldValue);
        // Apply the edits
        editor.apply();
    }


    public void getGoldComplete(double gold) {
        if(gold !=-1) {
            textGold.setText(String.format("You have\n\n%s\n\ngold", gold));
            lastGoldValue = "" + gold;
        } else {
            textGold.setText(String.format("You have\n\n%s\n\ngold", lastGoldValue.equals("")?0.0:lastGoldValue));
            errorMessage("Could not connect to bank");
        }
    }

    public void realtimeUpdateComplete(List<Message> messageList) {
        adapter = new BankRecyclerViewAdapter(this, messageList);
        bankView.setAdapter(adapter);
    }

    private void errorMessage(String errorText) {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), errorText, Snackbar.LENGTH_LONG);
        snackbar.show();
    }
}
