package com.example.s1616573.coinz;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class WalletActivity extends AppCompatActivity implements RecyclerViewAdapter.ItemClickListener {

    private String tag = "WalletActivity";
    private UserFirestore userFirestore;
    private FirebaseAuth mAuth;

    private RecyclerView walletView;
    private RecyclerViewAdapter adapter;
    private FloatingActionButton depositButton;
    private FloatingActionButton sendButton;
    private Toolbar toolbar;
    private View progressView;

    private final String preferencesFile = "MyPrefsFile";
    private final String USER_COLLECTION = "users";
    private HashMap<String, Double> rates;
    private HashMap<Integer, Coin> selectedCoins;
    private String userTo;

    private int noSelected;
    private int noDeposited;

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
        toolbar = findViewById(R.id.toolbar_wallet);
        setSupportActionBar(toolbar);
        ActionBar aBar = getSupportActionBar();

        if (aBar != null) {
            aBar.setDisplayHomeAsUpEnabled(true);
        }

        progressView = findViewById(R.id.deposit_progress);

        selectedCoins = new HashMap<>();

        depositButton.setOnClickListener(view -> {
            deposit();
        });

        sendButton.setOnClickListener(view -> {
            chooseUser();
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

        userFirestore.getNumberDeposited(this);
        noSelected = 0;
    }

    public void showCoins(List<Coin> coins) {
        for(Coin c : coins) {
            double value = c.getValue();
            double goldValue = value * rates.get(c.getCurrency());
            c.setGoldValue(goldValue);
        }
        adapter = new RecyclerViewAdapter(this, coins);
        adapter.setClickListener(this);
        walletView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(View view, int position) {
        // toggle selection
        view.setSelected(!view.isSelected());
        noSelected = view.isSelected()?noSelected+1:noSelected-1;
        Coin clicked = adapter.getItem(position);
        if (selectedCoins.containsKey(position)) {
            selectedCoins.remove(position);
        } else {
            selectedCoins.put(position, clicked);
        }
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

    public void setNoDeposited(int n) {
        noDeposited = n;
        if(noDeposited == -1) {
            errorMessage("Unable to connect to bank");
        } else {
            showButtons();
        }
    }

    private void deposit() {
        // TODO: Try to deposit with no internet
        double gold = calculateGold();
        showProgress(true);
        userFirestore.depositCoins(this, selectedCoins.values(), gold);
    }

    public void transactionSucceeded(Boolean success) {
        showProgress(false);
        if (success) {
            // if coins are depositted at to the number of coins depositted today
            noDeposited = noDeposited <25? noDeposited +selectedCoins.size():25;
            showButtons();
            adapter.removeItems(selectedCoins.keySet());
            selectedCoins.clear();
            noSelected = 0;
        } else {
            errorMessage("Unable to connect to network");
        }
    }

    // Send gold to the selected user
    private void send() {
        double gold = calculateGold();
        showProgress(true);
        userFirestore.sendCoins(this, userTo, selectedCoins.values(), gold);
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

    // TODO: make usernames lowercase
    //https://stackoverflow.com/questions/10903754/input-text-dialog-android
    private void chooseUser() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected
        input.setInputType(InputType.TYPE_CLASS_TEXT);

        // create dialog
        AlertDialog dialog = new AlertDialog.Builder(this).setTitle("Who do you want to send coins to?")
                .setView(input)
                .setPositiveButton("Send",null)
                .setNegativeButton("Cancel", (d, which) -> d.cancel())
                .show();

        Button pButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        pButton.setOnClickListener(view -> {
            String uName = input.getText().toString();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection(USER_COLLECTION)
                    .whereEqualTo("username", uName)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            QuerySnapshot qs = task.getResult();
                            if (!Objects.requireNonNull(qs).isEmpty()) {
                                if (!qs.equals(mAuth.getUid())) {
                                    for (DocumentSnapshot document : Objects.requireNonNull(qs)) {
                                        userTo = document.getId();
                                        Log.d(tag, "[chooseUser] " + userTo);
                                        send();
                                        dialog.dismiss();
                                    }
                                } else {
                                    builder.setMessage("[chooseUser] You can't send coins to yourself");
                                    Log.d(tag, "[chooseUser] Send to self");
                                }
                            } else {
                                builder.setMessage("[chooseUser] User does not exist");
                                Log.d(tag, "[chooseUser] user does not exist");
                            }
                        } else {
                            Log.d(tag, "[chooseUser] Error getting documents: ", task.getException());
                        }
                    });
        });
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
