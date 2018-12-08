package com.example.s1616573.coinz;

import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.firestore.WriteBatch;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class WalletFirestore extends UserFirestore {

    public WalletCompleteListener walletCompleteListener = null;

    private String tag = "WalletFirestore";
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private DocumentReference docRef;
    private String userID;
    private DocumentSnapshot document;
    private FirebaseFirestoreSettings settings;

    public WalletFirestore(FirebaseAuth mAuth) {
        super(mAuth);
        this.mAuth = mAuth;
        docRef = getDocRef();
        userID = getUserID();
        db = getDb();
        settings = getSettings();
    }

    public void getCoinsInWallet() {
        docRef.collection("wallet")
                .orderBy("dateCollected", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot walletDocument = task.getResult();
                        if (walletDocument != null && !walletDocument.isEmpty()) {
                            walletCompleteListener.getCoinsComplete(task.getResult().toObjects(Coin.class));
                        } else {
                            Log.d(tag, "[getCoinsInWallet] Wallet is empty: ", task.getException());
                        }
                    } else {
                        Log.d(tag, "[getCoinsInWallet] Error getting documents: ", task.getException());
                    }
                });
    }

    public void depositCoins(Collection<Coin> coins, double gold){
        db.runTransaction((Transaction.Function<Void>) transaction -> {
            DocumentSnapshot snapshot = transaction.get(docRef);
            double goldInBank = 0;
            if (snapshot.contains(GOLD_FIELD)) {
                goldInBank = snapshot.getDouble(GOLD_FIELD);
            }
            goldInBank += gold;
            transaction.update(docRef, GOLD_FIELD, goldInBank);
            int noDeposited = 0;
            if (snapshot.contains(NO_BANKED_FIELD)) {
                noDeposited = (int) Math.floor(snapshot.getDouble(NO_BANKED_FIELD));
            }
            transaction.update(docRef, NO_BANKED_FIELD, noDeposited + coins.size());
            for(Coin c : coins) {
                docRef.collection(WALLET_COLLECTION).document(c.getId())
                        .delete();
            }
            // Success
            return null;
        }).addOnSuccessListener(aVoid -> {
            Log.d(tag, "[depositCoins] Transaction success!");
            walletCompleteListener.transactionSucceeded(true);
        })
                .addOnFailureListener(e -> {
                    Log.w(tag, "[depositCoins] Transaction failure.", e);
                    walletCompleteListener.transactionSucceeded(false);
                });
    }

    public void sendCoins(String userTo, Collection<Coin> coins, double gold){
        // Get a new write batch
        WriteBatch batch = db.batch();

        DocumentReference sendRef = db.collection(USER_COLLECTION).document(userTo).collection("sent").document();

        Message message = new Message(userID, gold);
      //  Map<String, Double> sentGold = new HashMap<>();
        // TODO: change to username save in sharedprefs when logging in remove when log out?
       // sentGold.put(userID, gold);
        batch.set(sendRef,message);

        for(Coin c : coins) {
            batch.delete(docRef.collection(WALLET_COLLECTION).document(c.getId()));
        }
        // Commit the batch
        batch.commit().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(tag, "[sendCoins] Batch success!");
                walletCompleteListener.transactionSucceeded(true);
            } else {
                Log.w(tag, "[sendCoins] Batch failure.");
                walletCompleteListener.transactionSucceeded(false);
            }
        });
    }

    public void getNumberDeposited() {
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                document = task.getResult();
                assert document != null;
                if (document.exists()) {
                    Log.d(tag, "[getNumberDeposited] DocumentSnapshot data: " + document.getData());
                    int noDeposited = 0;
                    if (document.contains(NO_BANKED_FIELD)) {
                        noDeposited = (int)Math.floor(document.getDouble(NO_BANKED_FIELD));
                    }
                    walletCompleteListener.getNumberDepositedComplete(noDeposited);
                } else {
                    Log.d(tag, "[getNumberDeposited] No such document");
                    walletCompleteListener.getNumberDepositedComplete(0);
                }
            } else {
                Log.d(tag, "[getNumberDeposited] Get failed with ", task.getException());
                walletCompleteListener.getNumberDepositedComplete(-1);
            }
        });
    }

    // TODO: make usernames lowercase
    //https://stackoverflow.com/questions/10903754/input-text-dialog-android
    public void chooseUser() {
        AlertDialog.Builder builder = new AlertDialog.Builder((WalletActivity)walletCompleteListener);

        // Set up the input
        final EditText input = new EditText((WalletActivity)walletCompleteListener);
        // Specify the type of input expected
        input.setInputType(InputType.TYPE_CLASS_TEXT);

        // create dialog
        AlertDialog dialog = new AlertDialog.Builder((WalletActivity)walletCompleteListener).setTitle("Who do you want to send coins to?")
                .setView(input)
                .setPositiveButton("Send",null)
                .setNegativeButton("Cancel", (d, which) -> d.cancel())
                .show();

        Button pButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        pButton.setOnClickListener(view -> {
            String uName = input.getText().toString();
            db.collection(USER_COLLECTION)
                    .whereEqualTo("username", uName)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            QuerySnapshot qs = task.getResult();
                            if (!Objects.requireNonNull(qs).isEmpty()) {
                                if (!qs.equals(mAuth.getUid())) {
                                    for (DocumentSnapshot document : Objects.requireNonNull(qs)) {
                                        String userTo = document.getId();
                                        Log.d(tag, "[chooseUser] " + userTo);
                                        walletCompleteListener.chooseUserComplete(userTo);

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
}
