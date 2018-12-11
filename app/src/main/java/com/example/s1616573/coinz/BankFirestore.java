package com.example.s1616573.coinz;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class BankFirestore extends UserFirestore {

    public BankCompleteListener bankCompleteListener = null;

    private String tag = "BankFirestore";
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private DocumentReference docRef;
    private String userID;
    private DocumentSnapshot document;
    private FirebaseFirestoreSettings settings;

    private DocumentReference messageRef;
    private ListenerRegistration registration;
    private HashMap<String, Message> messages = new HashMap<>();


    BankFirestore(FirebaseAuth mAuth) {
        super(mAuth);
        this.mAuth = mAuth;
        docRef = getDocRef();
        userID = getUserID();
        db = getDb();
        settings = getSettings();

        messageRef = db.collection(USER_COLLECTION).document(Objects.requireNonNull(userID));
    }

    // Get the amount of gold the user has in their bank
    public void getGoldInBank() {
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                document = task.getResult();
                assert document != null;
                if (document.exists()) {
                    Log.d(tag, "[getGoldInBank] DocumentSnapshot data: " + document.getData());
                    double goldInBank = 0;
                    if (document.contains(GOLD_FIELD)) {
                        goldInBank = document.getDouble(GOLD_FIELD);
                    }
                    // pass result back to bank activity
                    bankCompleteListener.getGoldComplete(goldInBank);
                } else {
                    Log.d(tag, "[getGoldInBank] No such document");
                    bankCompleteListener.getGoldComplete(0);
                }
            } else {
                Log.d(tag, "[getGoldInBank] get failed with ", task.getException());
                bankCompleteListener.getGoldComplete(-1);
            }
        });
    }

    public void realtimeUpdateListener() {
        List<Message> messageList = new ArrayList<>();
        registration = messageRef.collection(SENT_COLLECTION)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.w(tag, "[realtimeUpdateListener] Listen failed.", e);
                        return;
                    }
                    db.runTransaction((Transaction.Function<Void>) transaction -> {
                        List<DocumentChange> docChanges = snapshots.getDocumentChanges();
                        String sender;
                        double gold;
                        double goldInBank = 0;

                        DocumentSnapshot transactionSnapshot = transaction.get(docRef);
                        if (transactionSnapshot.contains(GOLD_FIELD)) {
                            goldInBank = transactionSnapshot.getDouble(GOLD_FIELD);
                        }
                        messageList.clear();
                        for (DocumentChange dc : docChanges) {
                            if(dc.getType() == DocumentChange.Type.ADDED) {
                                DocumentSnapshot d = dc.getDocument();
                                sender = d.getString(SENDER_FIELD);
                                gold = d.getDouble(GOLD_FIELD);
                                if (!messages.containsKey(d.getId())) {
                                    goldInBank += gold;
                                    Message m = new Message(sender, gold);
                                    messages.put(d.getId(), m);
                                    messageList.add(m);
                                   // messageRef.collection(SENT_COLLECTION).document(d.getId()).delete();
                                }
                            }
                        }
                        transaction.update(docRef, GOLD_FIELD, goldInBank);
                        return null;
                    }).addOnSuccessListener(aVoid -> {
                        Log.d(tag, "[realtimeUpdateListener] Transaction succeeded");
                        bankCompleteListener.realtimeUpdateComplete(messageList);
                    }).addOnFailureListener(er -> {
                        Log.d(tag, "[realtimeUpdateListener] Transaction failed.", er);
                    });

                });

    }

    public void stopListening() {
        registration.remove();
    }

    public void clearMessages() {
        Task qs = messageRef.collection(SENT_COLLECTION)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        WriteBatch batch = db.batch();
                        for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                            batch.delete(messageRef.collection(SENT_COLLECTION).document(document.getId()));
                        }
                        // Commit the batch
                        batch.commit().addOnCompleteListener(task2 -> {
                            if (task2.isSuccessful()) {
                                Log.d(tag, "[clearMessages] Batch success!");
                            } else {
                                Log.w(tag, "[clearMessages] Batch failure.");
                            }
                        });
                    } else {
                        Log.d(tag, "[clearMessages] Error getting documents: ", task.getException());
                    }
                });
    }
}
