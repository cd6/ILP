package com.example.s1616573.coinz;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

// Class to perform all firestore accesses in the MainActivity
public class MainFirestore extends UserFirestore {

    public DownloadCompleteListener downloadCompleteListener = null;

    private String tag = "MainFirestore";
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private DocumentReference docRef;
    private String userID;
    private DocumentSnapshot document;
    private FirebaseFirestoreSettings settings;

    private ArrayList<String> pickedUpCoins;

    public MainFirestore(FirebaseAuth mAuth) {
        super(mAuth);
        this.mAuth = mAuth;
        docRef = getDocRef();
        userID = getUserID();
        db = getDb();
        settings = getSettings();
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
            downloadCompleteListener.downloadComplete(pickedUpCoins);
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
}
