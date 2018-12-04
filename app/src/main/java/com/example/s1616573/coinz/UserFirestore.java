package com.example.s1616573.coinz;

import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.firestore.WriteBatch;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class UserFirestore {

    // TODO: check internet connection before accessing to avoid crash

    private String tag = "UserFirestore";
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private DocumentReference docRef;
    private String userID;
    private DocumentSnapshot document;

    private final String USER_COLLECTION = "users";
    private final String USER_PRIVATE = "user";
    private final String USER_DOCUMENT = "userDoc";
    private final String WALLET_COLLECTION = "wallet";
    private final String PICKED_UP_COINS_FIELD = "pickedUpCoins";
    private final String GOLD_FIELD = "gold";
    private final String NO_BANKED_FIELD = "noBanked";

    private ArrayList<String> pickedUpCoins;

    public DownloadCompleteListener listener = null;

    public UserFirestore(FirebaseAuth mAuth) {
        // Access a Cloud Firestore instance from your Activity
        this.mAuth = mAuth;
        userID = mAuth.getUid();
        db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        db.setFirestoreSettings(settings);
        docRef = db.collection(USER_COLLECTION).document(Objects.requireNonNull(userID)).collection(USER_PRIVATE).document(USER_DOCUMENT);
    }

    public void realtimeUpdateListener() {
        docRef.addSnapshotListener(((documentSnapshot, e) -> {
            if(e != null) {
                Log.e(tag, e.getMessage());
            } else if (documentSnapshot != null && documentSnapshot.exists()) {

            }
        }));
    }

    public void getPickedUpCoins() {
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                document = task.getResult();
                assert document != null;
                if (document.exists()) {
                    Log.d(tag, "[checkFirstLoginToday] DocumentSnapshot data: " + document.getData());
                    Date lastLoginDate = document.getDate("lastLogin");
                    LocalDate today = LocalDate.now();
                    Date todayDate = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant());
                    // if the user has not logged in today
                    if (lastLoginDate != null && lastLoginDate.compareTo(todayDate) < 0) {
                        // Empty list of coins picked up by the user
                        resetPickedUpCoins();
                    } else {
                        setPickedUpCoins((ArrayList<String>) document.get(PICKED_UP_COINS_FIELD));
                    }
                } else {
                    Log.d(tag, "[checkFirstLoginToday] No such document");
                }
            } else {
                Log.d(tag, "get failed with ", task.getException());
            }
            docRef.update("lastLogin", Timestamp.now());
            listener.downloadComplete(pickedUpCoins);
        });
    }

    private void setPickedUpCoins(ArrayList<String> pickedUpCoins) {
        this.pickedUpCoins = pickedUpCoins;
    }

    private void resetPickedUpCoins() {
        // Remove all coin IDs from array of coins picked up from the map
        // Remove the 'capital' field from the document
        Map<String,Object> updates = new HashMap<>();
        updates.put(PICKED_UP_COINS_FIELD, FieldValue.delete());

        // set number of coins banked today to 0
        updates.put(NO_BANKED_FIELD,  FieldValue.delete());

        docRef.update(updates).addOnCompleteListener(aVoid -> Log.d(tag, "[resetPickedUpCoins] Update successful"));
    }

    public void pickUp(Coin coin) {
        // pickedUpCoins stores ID of coins for the map of the current day that the user has picked up
        docRef.update(PICKED_UP_COINS_FIELD, FieldValue.arrayUnion(coin.getId()));
        // Store when the coin was collected to order them in wallet by when the coins were picked up
        coin.setDate(Timestamp.now());
        docRef.collection(WALLET_COLLECTION).document(coin.getId()).set(coin)
                .addOnSuccessListener(aVoid -> Log.d(tag, "[pickUp] DocumentSnapshot successfully written!"))
                .addOnFailureListener(e -> Log.w(tag, "[pickUp] Error writing document", e));
    }


    public void getCoinsInWallet(WalletActivity walletActivity) {
        docRef.collection("wallet")
                .orderBy("dateCollected", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot walletDocument = task.getResult();
                        if (walletDocument != null && !walletDocument.isEmpty()) {
                            walletActivity.showCoins(task.getResult().toObjects(Coin.class));
                        } else {
                            Log.d(tag, "[getCoinsInWallet] Wallet is empty: ", task.getException());
                        }
                    } else {
                        Log.d(tag, "[getCoinsInWallet] Error getting documents: ", task.getException());
                    }
                });
    }

    public void emptyWallet() {
        Task qs = docRef.collection(WALLET_COLLECTION)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        WriteBatch batch = db.batch();
                        for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                            batch.delete(docRef.collection(WALLET_COLLECTION).document(document.getId()));
                        }
                        // Commit the batch
                        batch.commit().addOnCompleteListener(task2 -> {
                            if (task2.isSuccessful()) {
                                Log.d(tag, "[emptyWallet] Batch success!");
                            } else {
                                Log.w(tag, "[emptyWallet] Batch failure.");
                            }
                        });
                    } else {
                        Log.d(tag, "[emptyWallet] Error getting documents: ", task.getException());
                    }
                });
    }

    public void depositCoins(WalletActivity walletActivity, Collection<Coin> coins, double gold){
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
            walletActivity.transactionSucceeded(true);
        })
                .addOnFailureListener(e -> {
                    Log.w(tag, "[depositCoins] Transaction failure.", e);
                    walletActivity.transactionSucceeded(false);
                });
    }

    public void sendCoins(WalletActivity walletActivity, String userTo, Collection<Coin> coins, double gold){
        // Get a new write batch
        WriteBatch batch = db.batch();

        DocumentReference sendRef = db.collection(USER_COLLECTION).document(userTo).collection("sent").document();
        Map<String, Double> sentGold = new HashMap<>();
        // TODO: change to username save in sharedprefs when logging in remove when log out?
        sentGold.put(userID, gold);
        batch.set(sendRef,sentGold);

        for(Coin c : coins) {
            batch.delete(docRef.collection(WALLET_COLLECTION).document(c.getId()));
        }
        // Commit the batch
        batch.commit().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(tag, "[sendCoins] Batch success!");
                walletActivity.transactionSucceeded(true);
            } else {
                Log.w(tag, "[sendCoins] Batch failure.");
                walletActivity.transactionSucceeded(false);
            }
        });
    }

    public void getGoldInBank(BankActivity bankActivity) {
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                document = task.getResult();
                assert document != null;
                if (document.exists()) {
                    Log.d(tag, "[depositGold] DocumentSnapshot data: " + document.getData());
                    double goldInBank = 0;
                    if (document.contains(GOLD_FIELD)) {
                        goldInBank = document.getDouble(GOLD_FIELD);
                    }
                    bankActivity.showGold(goldInBank);
                } else {
                    Log.d(tag, "[depositGold] No such document");
                    bankActivity.showGold(0);
                }
            } else {
                Log.d(tag, "get failed with ", task.getException());
                bankActivity.showGold(-1);
            }
        });
    }

    public void getNumberDeposited(WalletActivity walletActivity) {
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
                    walletActivity.setNoDeposited(noDeposited);
                } else {
                    Log.d(tag, "[depositGold] No such document");
                    walletActivity.setNoDeposited(0);
                }
            } else {
                Log.d(tag, "get failed with ", task.getException());
                walletActivity.setNoDeposited(-1);
            }
        });
    }
}
