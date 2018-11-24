package com.example.s1616573.coinz;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

public class WalletActivity extends AppCompatActivity implements RecyclerViewAdapter.ItemClickListener {

    private String tag = "WalletActivity";
    private UserFirestore userFirestore;
    private FirebaseAuth mAuth;
    private RecyclerView walletView;
    private RecyclerViewAdapter adapter;
    private Button depositButton;
    private final String preferencesFile = "MyPrefsFile";
    private int noSelected = 0;
    private HashMap<String, Double> rates;
    private HashMap<Integer, Coin> selectedCoins;
    private Toolbar toolbar;
    private View progressView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);
        walletView = findViewById(R.id.rv_coins);
        walletView.setLayoutManager(new LinearLayoutManager(this));
        depositButton = findViewById(R.id.deposit_button);
        depositButton.setEnabled(false);
        toolbar = findViewById(R.id.toolbar_wallet);
        setSupportActionBar(toolbar);
        ActionBar aBar = getSupportActionBar();
        aBar.setDisplayHomeAsUpEnabled(true);

        progressView = findViewById(R.id.deposit_progress);

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
        walletView.setAdapter(adapter);
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
        // change 25 to 25-number deposited that day
        depositButton.setEnabled(!selectedCoins.isEmpty() && selectedCoins.size() <=25);
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
        // TODO: Try to deposit with no internet
        double gold = 0;
        for (Integer p : selectedCoins.keySet()) {
            Coin c = selectedCoins.get(p);
            gold += c.getValue()*rates.get(c.getCurrency());
        }
        showProgress(true);
        userFirestore.depositCoins(this, selectedCoins.values(), gold);
    }

    public void depositSucceeded(Boolean success) {
        showProgress(false);
        if (success) {
            adapter.removeItems(selectedCoins.keySet());
            selectedCoins.clear();
        } else {
            errorMessage("Unable to connect to network");
        }
    }

    private void errorMessage(String errorText) {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), errorText, Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        walletView.setVisibility(show ? View.GONE : View.VISIBLE);
        walletView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                walletView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        progressView.setVisibility(show ? View.VISIBLE : View.GONE);
        progressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

}
