package com.example.s1616573.coinz;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

public class WalletActivity extends AppCompatActivity implements WalletRecyclerViewAdapter.ItemClickListener, WalletCompleteListener {

    private String tag = "WalletActivity";
    private WalletFirestore walletFirestore;
    private FirebaseAuth mAuth;

    private RecyclerView walletView;
    private WalletRecyclerViewAdapter adapter;
    private FloatingActionButton depositButton;
    private FloatingActionButton sendButton;
    private View progressView;

    private HashMap<String, Double> rates;
    private HashMap<Integer, Coin> selectedCoins;

    private int noSelected;
    private int noDeposited;
    private String username;

    @SuppressLint("UseSparseArrays")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);
        walletView = findViewById(R.id.rv_coins);
        walletView.setLayoutManager(new LinearLayoutManager(this));
        depositButton = findViewById(R.id.fab_deposit);
        depositButton.setClickable(false);
        sendButton = findViewById(R.id.fab_send);
        sendButton.setClickable(false);
        Toolbar toolbar = findViewById(R.id.toolbar_wallet);
        setSupportActionBar(toolbar);
        ActionBar aBar = getSupportActionBar();

        // add back button to action bar which takes the user back to the map
        if (aBar != null) {
            aBar.setDisplayHomeAsUpEnabled(true);
        }

        progressView = findViewById(R.id.deposit_progress);
        selectedCoins = new HashMap<>();

        showButtons();

        depositButton.setOnClickListener(view ->
            deposit()
        );

        sendButton.setOnClickListener(view ->
            walletFirestore.chooseUser(username)
        );
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth = FirebaseAuth.getInstance();
        walletFirestore = new WalletFirestore(mAuth);
        walletFirestore.walletCompleteListener = this;
        walletFirestore.getCoinsInWallet();

        // Restore preferences
        String preferencesFile = "MyPrefsFile";
        SharedPreferences settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE);
        String geoJsonCoins = settings.getString("coinMap", "");

        String userPreferencesFile = mAuth.getUid();
        SharedPreferences userSettings = getSharedPreferences(userPreferencesFile, Context.MODE_PRIVATE);
        username = userSettings.getString("username", "");
        Log.d(tag, "[onStart] username is " + username);
        if(username.equals("")){
            walletFirestore.getUsername();
        }

        try {
            getRates(geoJsonCoins);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        walletFirestore.getNumberDeposited();
        noSelected = 0;
    }

    @Override
    public void onStop() {
        super.onStop();

        if (username != null) {
            SharedPreferences settings = getSharedPreferences(mAuth.getUid(), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("username", username);
            editor.apply();
        }
    }

    public void getUsernameComplete(String username) {
        this.username = username;
        Log.d(tag, "[getUsernameComplete] Username = " + username);
    }

    // Display coins in recycler view
    public void getCoinsComplete(List<Coin> coins) {
        // Calculate gold value for every coin using today's exchange rates
        for(Coin c : coins) {
            double value = c.getValue();
            double goldValue = value * rates.get(c.getCurrency());
            c.setGoldValue(goldValue);
        }
        adapter = new WalletRecyclerViewAdapter(this, coins);
        adapter.setClickListener(this);
        walletView.setAdapter(adapter);
    }

    @Override
    // select/deselect coins from recycler view
    public void onItemClick(View view, int position) {
        // toggle selection
        view.setSelected(!view.isSelected());
        Coin clicked = adapter.getItem(position);
        // add or remove coin from list of selected coins
        if (selectedCoins.containsKey(position)) {
            selectedCoins.remove(position);
        } else {
            selectedCoins.put(position, clicked);
        }
        noSelected = selectedCoins.size();
        showButtons();
    }

    // if less than 25 coins have been deposited, show deposit button else send
    private void showButtons() {
        Log.d(tag, "[showButtons] noSelected = " + noSelected);
        Log.d(tag, "[showButtons] noDeposited = " + noDeposited);
        if(noSelected > 0) {
            // show deposit button if some coins are selected and less than 25 have been deposited
            if(noDeposited + noSelected <= 25) {
                depositButton.show();
                sendButton.hide();
            } else if(noDeposited >= 25){
                depositButton.hide();
                sendButton.show();
            }
        } else {
            depositButton.hide();
            sendButton.hide();
        }
    }

    private void getRates(String geoJson) throws JSONException {
        JSONObject obj = new JSONObject(geoJson);
        rates = new HashMap<>();
        String[] currencies = new String[]{"SHIL","DOLR","QUID","PENY"};
        for (String c : currencies) {
            String rate = obj.getJSONObject("rates").getString(c);
            rates.put(c, Double.parseDouble(rate));
        }
    }

    public void getNumberDepositedComplete(int n) {
        noDeposited = n;
        if(noDeposited == -1) {
            errorMessage("Unable to connect to bank");
        } else {
            showButtons();
        }
    }

    // Deposit selected coins to the bank
    private void deposit() {
        // TODO: Try to deposit with no internet
        double gold = calculateGold();
        showProgress(true);
        walletFirestore.depositCoins(selectedCoins.values(), gold);
    }

    // calculate total value of coins selected in gold
    private double calculateGold() {
        double gold = 0;
        for (Integer p : selectedCoins.keySet()) {
            Coin c = selectedCoins.get(p);
            gold += c.getGoldValue();
        }
        return gold;
    }

    public void transactionSucceeded(Boolean success) {
        showProgress(false);
        if (success) {
            // if coins are deposited at to the number of coins deposited today
            noDeposited = noDeposited <25? noDeposited + selectedCoins.size():25;
            adapter.removeItems(selectedCoins.keySet());
            selectedCoins.clear();
            noSelected = 0;
            showButtons();
        } else {
            errorMessage("Unable to connect to network");
        }
    }

    // Send gold to the selected user
    public void chooseUserComplete(String userTo) {
        double gold = calculateGold();
        showProgress(true);
        walletFirestore.sendCoins(username, userTo, selectedCoins.values(), gold);
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
