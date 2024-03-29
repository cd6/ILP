package com.example.s1616573.coinz;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class BankActivity extends AppCompatActivity implements BankCompleteListener{

    private String tag = "BankActivity";
    private BankFirestore bankFirestore;
    private TextView textGold;
    private TextView textLevel;
    private RecyclerView bankView;

    private String preferencesFile = "";
    private String lastGoldValue = "";
    private double gold;
    private String goldText = "You have\n\n%.2f\n\ngold!";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bank);
        bankView = findViewById(R.id.rv_messages);
        bankView.setLayoutManager(new LinearLayoutManager(this));
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // back button to return to map
        ActionBar aBar = getSupportActionBar();
        assert aBar != null;
        aBar.setDisplayHomeAsUpEnabled(true);

        textGold = findViewById(R.id.text_gold);
        textLevel = findViewById(R.id.text_level);
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        bankFirestore = new BankFirestore(mAuth.getUid());
        bankFirestore.bankCompleteListener = this;
        // get gold
        bankFirestore.getGoldInBank();

        preferencesFile = mAuth.getUid();

        // Restore preferences
        SharedPreferences settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE);

        // use "" as the default value (this might be the first time the app is run)
        lastGoldValue = settings.getString("lastGoldValue", "");
        Log.d(tag, "[onStart] lastGoldValue = " + lastGoldValue);

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

        // stop listening for updates and remove messages
        bankFirestore.stopListening();
        bankFirestore.clearMessages();
    }


    @SuppressLint("DefaultLocale")
    public void getGoldComplete(double gold) {
        // If there was an error retrieving gold display the last value stored on the device
        if(gold !=-1) {
            // Only show gold to 2 significant figures
            this.gold = gold;
            textGold.setText(String.format(goldText, gold));
            lastGoldValue = "" + gold;
            Log.d(tag, "[getGoldComplete] lastGoldValue = " + lastGoldValue);
        } else {
            this.gold = Double.parseDouble(lastGoldValue);
            textGold.setText(String.format(goldText, lastGoldValue.equals("")?0.0:lastGoldValue));
            errorMessage();
        }
        displayLevel();
    }


    public void realtimeUpdateComplete(List<Message> messages) {
        // add the gold from messages to the text on screen to match bank value
        for(Message m : messages) {
            gold += m.getGold();
        }
        textGold.setText(String.format(goldText,gold));

        // display messages
        BankRecyclerViewAdapter adapter = new BankRecyclerViewAdapter(this, messages);
        bankView.setAdapter(adapter);

        displayLevel();
    }

    private void displayLevel(){
        if(gold < 10000) {
            textLevel.setText("Rank:\nStudent");
        } else if (gold < 1000000) {
            textLevel.setText("Rank:\nIntern");
        } else if (gold < 10000000) {
            textLevel.setText("Rank:\nDeveloper");
        } else if (gold < 1000000000) {
            textLevel.setText("Rank:\nManager");
        } else if (gold < 100000000000.0) {
            textLevel.setText("Rank:\nDirector");
        } else {
            textLevel.setText("Rank:\nCTO");
        }
    }

    private void errorMessage() {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Could not connect to bank", Snackbar.LENGTH_LONG);
        snackbar.show();
    }
}
