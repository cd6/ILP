package com.example.s1616573.coinz;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class UserFirestore {

    // TODO: check internet connection before accessing to avoid crash

    private String tag = "UserFirestore";
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private DocumentReference docRef;
    private String userID;
    private DocumentSnapshot document;
    private FirebaseFirestoreSettings settings;

    final String USER_COLLECTION = "users";
    final String USER_PRIVATE = "user";
    final String USER_DOCUMENT = "userDoc";
    final String WALLET_COLLECTION = "wallet";
    final String SENT_COLLECTION = "sent";
    final String PICKED_UP_COINS_FIELD = "pickedUpCoins";
    final String GOLD_FIELD = "gold";
    final String NO_BANKED_FIELD = "noBanked";
    final String USER_FROM_FIELD = "userFrom";

    public ReceivedCoinsListener receivedCoinsListener = null;

    public UserFirestore(FirebaseAuth mAuth) {
        // Access a Cloud Firestore instance from your Activity
        this.mAuth = mAuth;
        userID = mAuth.getUid();
        db = FirebaseFirestore.getInstance();
        settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        db.setFirestoreSettings(settings);
        docRef = db.collection(USER_COLLECTION).document(Objects.requireNonNull(userID)).collection(USER_PRIVATE).document(USER_DOCUMENT);
    }

    DocumentReference getDocRef() {
        return docRef;
    }

    String getUserID() {
        return userID;
    }

    FirebaseFirestore getDb() {
        return db;
    }

    FirebaseFirestoreSettings getSettings() {
        return settings;
    }

    public void realtimeUpdateListener(Context context) {
        DocumentReference docRefRealtime = db.collection(USER_COLLECTION).document(Objects.requireNonNull(userID));
        docRefRealtime.collection(SENT_COLLECTION)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.w(tag, "[realtimeUpdateListener] Listen failed.", e);
                        return;
                    }
                    List<DocumentChange> docChanges = snapshots.getDocumentChanges();
                    List<Message> messageList = new ArrayList<>();
                    String sender;
                    double gold;
                    double totalGold = 0;
                    for (DocumentChange dc : snapshots.getDocumentChanges()) {
                        DocumentSnapshot d = dc.getDocument();
                        sender = d.getString(USER_FROM_FIELD);
                        gold = d.getDouble(GOLD_FIELD);
                        totalGold += gold;
                        messageList.add(new Message(sender, gold));
                    }
                });
    }
}
