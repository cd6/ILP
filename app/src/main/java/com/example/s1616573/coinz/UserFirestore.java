package com.example.s1616573.coinz;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class UserFirestore {

    private String tag = "UserFirestore";
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private DocumentReference docRef;

    public UserFirestore(FirebaseAuth mAuth) {
        // Access a Cloud Firestore instance from your Activity
        this.mAuth = mAuth;
        db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

        // check if this is the first time the user has logged in today
        checkFirstLoginToday();
    }

    private void checkFirstLoginToday() {
        AtomicBoolean first = new AtomicBoolean(false);
        docRef = db.collection("users").document(Objects.requireNonNull(mAuth.getUid()));
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                assert document != null;
                if (document.exists()) {
                    Log.d(tag, "DocumentSnapshot data: " + document.getData());
                    Date lastLoginDate = document.getDate("lastLogin");
                    LocalDate today = LocalDate.now();
                    Date todayDate = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant());
                    // if the user has not logged in today
                    if (lastLoginDate != null && lastLoginDate.compareTo(todayDate) < 0) {
                        // Empty list of coins picked up by the user
                        resetPickedUpCoins();
                    }
                } else {
                    Log.d(tag, "No such document");
                }
            } else {
                Log.d(tag, "get failed with ", task.getException());
            }
        });
        docRef.update("lastLogin", Timestamp.now());

    }

    private void resetPickedUpCoins() {
        // Remove all coin IDs from array of coins picked up from the map
        // Remove the 'capital' field from the document
        Map<String,Object> updates = new HashMap<>();
        updates.put("pickedUpCoins", FieldValue.delete());

        docRef.update(updates).addOnCompleteListener(aVoid -> Log.d(tag, "resetPickedUpCoins: Update successful"));
    }

    public void pickUp(Coin coin) {
        // pickedUpCoins stores ID of coins for the map of the current day that the user has picked up
        docRef.update("pickedUpCoins", FieldValue.arrayUnion(coin.getId()));
        db.collection("users").document(Objects.requireNonNull(mAuth.getUid()))
                .collection("wallet").document(coin.getId()).set(coin)
                .addOnSuccessListener(aVoid -> Log.d(tag, "pickUp: DocumentSnapshot successfully written!"))
                .addOnFailureListener(e -> Log.w(tag, "pickUp: Error writing document", e));
    }
}
