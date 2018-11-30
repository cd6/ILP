package com.example.s1616573.coinz;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.TestLooperManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
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
    private String preferencesFile = "";
    private String lastGoldValue = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bank);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar aBar = getSupportActionBar();
        aBar.setDisplayHomeAsUpEnabled(true);

        textGold = findViewById(R.id.text_gold);
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth = FirebaseAuth.getInstance();
        userFirestore = new UserFirestore(mAuth);
        userFirestore.getGoldInBank(this);

        preferencesFile = mAuth.getUid();

        // Restore preferences
        SharedPreferences settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE);

        // use "" as the default value (this might be the first time the app is run)
        lastGoldValue = settings.getString("lastGoldValue", "");
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

    public void showGold(double gold) {
        if(gold !=-1) {
            textGold.setText(String.format("You have\n\n%s\n\ngold", gold));
            lastGoldValue = "" + gold;
        } else {
            textGold.setText(String.format("You have\n\n%s\n\ngold", lastGoldValue.equals("")?0.0:lastGoldValue));
            errorMessage("Could not connect to bank");
        }
    }

    private void errorMessage(String errorText) {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), errorText, Snackbar.LENGTH_LONG);
        snackbar.show();
    }
}
