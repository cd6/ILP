package com.example.s1616573.coinz;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class LoginFirestore extends UserFirestore {

    private String tag = "LoginFirestore";
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private DocumentReference docRef;
    private String userID;
    private DocumentSnapshot document;
    private FirebaseFirestoreSettings settings;

    public LoginFirestore(FirebaseAuth mAuth) {
        super(mAuth);
        this.mAuth = mAuth;
        docRef = getDocRef();
        userID = getUserID();
        db = getDb();
        settings = getSettings();
    }

    public void createUser(String username) {
        String userID = mAuth.getUid();
        DocumentReference docRef = db.collection(USER_COLLECTION).document(Objects.requireNonNull(userID)).collection(USER_PRIVATE).document(USER_DOCUMENT);
        Map<String, Object> m = new HashMap<>();
        m.put("username", username);
        docRef.set(m)
                .addOnSuccessListener(aVoid -> Log.d(tag, "[createUser] DocumentSnapshot successfully written!"))
                .addOnFailureListener(e -> Log.w(tag, "[createUser] Error writing document", e));
    }
}
