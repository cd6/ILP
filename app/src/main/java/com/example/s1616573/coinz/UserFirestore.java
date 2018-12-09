package com.example.s1616573.coinz;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import java.util.Objects;

abstract class UserFirestore {

    // TODO: check internet connection before accessing to avoid crash

    private FirebaseFirestore db;
    private DocumentReference docRef;
    private String userID;
    private FirebaseFirestoreSettings settings;

    final String USER_COLLECTION = "users";
    @SuppressWarnings("WeakerAccess")
    final String USER_PRIVATE = "user";
    @SuppressWarnings("WeakerAccess")
    final String USER_DOCUMENT = "userDoc";
    final String WALLET_COLLECTION = "wallet";
    final String SENT_COLLECTION = "sent";
    final String PICKED_UP_COINS_FIELD = "pickedUpCoins";
    final String GOLD_FIELD = "gold";
    final String NO_BANKED_FIELD = "noBanked";
    final String SENDER_FIELD = "sender";

    UserFirestore(FirebaseAuth mAuth) {
        // Access a Cloud Firestore instance from your Activity
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


}
