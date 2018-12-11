package com.example.s1616573.coinz;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class LoginFirestore {

    LoginCompleteListener loginCompleteListener = null;

    private String tag = "LoginFirestore";
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private final String USER_COLLECTION = "users";


    LoginFirestore(FirebaseAuth mAuth) {
        this.mAuth = mAuth;
        db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        db.setFirestoreSettings(settings);
    }

    // create a new document on firestore for the new user
    public void createUser(String username) {
        String userID = mAuth.getUid();
        DocumentReference docRef = db.collection(USER_COLLECTION).document(Objects.requireNonNull(userID));
        Map<String, Object> m = new HashMap<>();
        m.put("username", username);
        docRef.set(m)
                .addOnSuccessListener(aVoid -> Log.d(tag, "[createUser] DocumentSnapshot successfully written!"))
                .addOnFailureListener(e -> Log.w(tag, "[createUser] Error writing document", e));
        String USER_PRIVATE = "user";
        String USER_DOCUMENT = "userDoc";
        docRef.collection(USER_PRIVATE).document(USER_DOCUMENT).set(new HashMap<>())
                .addOnCompleteListener(aVoid -> Log.d(tag, "[createUser] Update successful"));
    }

    // check if there is another user with the same username
    public void usernameAvailable(String username) {
        db.collection(USER_COLLECTION)
                .whereEqualTo("username", username)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot qs = task.getResult();
                        if (!Objects.requireNonNull(qs).isEmpty()) {
                            for (DocumentSnapshot document : Objects.requireNonNull(qs)) {
                                String user = document.getId();
                                    Log.d(tag, "[userAlreadyExists] " + user);
                                    loginCompleteListener.checkUsernameComplete(false);
                                }
                        } else {
                            Log.d(tag, "[userAlreadyExists] user does not exist");
                            loginCompleteListener.checkUsernameComplete(true);
                        }
                    } else {
                        Log.d(tag, "[userAlreadyExists] Error getting documents: ", task.getException());
                    }
                });
    }
}
