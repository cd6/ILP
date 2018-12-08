package com.example.s1616573.coinz;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

public class BankFirestore extends UserFirestore {

    public BankCompleteListener bankCompleteListener = null;

    private String tag = "BankFirestore";
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private DocumentReference docRef;
    private String userID;
    private DocumentSnapshot document;
    private FirebaseFirestoreSettings settings;

    public BankFirestore(FirebaseAuth mAuth) {
        super(mAuth);
        this.mAuth = mAuth;
        docRef = getDocRef();
        userID = getUserID();
        db = getDb();
        settings = getSettings();
    }

    // Get the amount of gold the user has in their bank
    public void getGoldInBank() {
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
                    // pass result back to bank activity
                    bankCompleteListener.getGoldComplete(goldInBank);
                } else {
                    Log.d(tag, "[depositGold] No such document");
                    bankCompleteListener.getGoldComplete(0);
                }
            } else {
                Log.d(tag, "get failed with ", task.getException());
                bankCompleteListener.getGoldComplete(-1);
            }
        });
    }


}
