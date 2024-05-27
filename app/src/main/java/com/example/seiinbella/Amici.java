package com.example.seiinbella;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Amici extends AppCompatActivity {

    private EditText emailInput;
    private Button addFriendButton;
    private RecyclerView requestsRecyclerView;
    private RecyclerView friendsRecyclerView;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private RequestsAdapter requestsAdapter;
    private FriendsAdapter friendsAdapter;
    private List<FriendRequest> friendRequestsList;
    private List<String> friendsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_amici);

        emailInput = findViewById(R.id.email_input);
        addFriendButton = findViewById(R.id.add_friend_button);
        requestsRecyclerView = findViewById(R.id.requests_recycler_view);
        friendsRecyclerView = findViewById(R.id.friends_recycler_view);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        friendRequestsList = new ArrayList<>();
        friendsList = new ArrayList<>();

        // Configura l'adapter per le richieste di amicizia
        requestsAdapter = new RequestsAdapter(friendRequestsList, new OnRequestActionListener() {
            @Override
            public void onRequestAction(String requestId) {
                acceptRequest(requestId);
            }
        }, new OnRequestActionListener() {
            @Override
            public void onRequestAction(String requestId) {
                declineRequest(requestId);
            }
        });
        requestsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        requestsRecyclerView.setAdapter(requestsAdapter);

        // Configura l'adapter per la lista degli amici
        friendsAdapter = new FriendsAdapter(friendsList, new FriendsAdapter.OnFriendActionListener() {
            @Override
            public void onFriendAction(String friendEmail) {
                removeFriend(friendEmail);
            }
        });
        friendsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        friendsRecyclerView.setAdapter(friendsAdapter);

        // Imposta l'azione per il pulsante di aggiunta amici
        addFriendButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            if (!TextUtils.isEmpty(email)) {
                sendFriendRequest(email);
            } else {
                Toast.makeText(Amici.this, "Inserisci un'email", Toast.LENGTH_SHORT).show();
            }
        });

        // Carica le richieste di amicizia e la lista degli amici
        loadFriendRequests();
        loadFriends();
    }

    // Funzione per inviare una richiesta di amicizia
    private void sendFriendRequest(String email) {
        Map<String, Object> friendRequest = new HashMap<>();
        friendRequest.put("da", currentUser.getEmail());
        friendRequest.put("a", email);
        friendRequest.put("stato", "in sospeso");

        db.collection("richiesteAmicizia")
                .add(friendRequest)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(Amici.this, "Richiesta di amicizia inviata", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(Amici.this, "Errore nell'invio della richiesta di amicizia", Toast.LENGTH_SHORT).show();
                    Log.e("Amici", "Errore nell'invio della richiesta di amicizia", e);
                });
    }

    // Funzione per caricare le richieste di amicizia
    private void loadFriendRequests() {
        db.collection("richiesteAmicizia")
                .whereEqualTo("a", currentUser.getEmail())
                .whereEqualTo("stato", "in sospeso")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        friendRequestsList.clear();
                        for (DocumentSnapshot document : task.getResult()) {
                            FriendRequest request = document.toObject(FriendRequest.class);
                            request.setRequestId(document.getId());
                            friendRequestsList.add(request);
                        }
                        requestsAdapter.notifyDataSetChanged();
                    } else {
                        Log.e("Amici", "Errore nel recupero delle richieste di amicizia", task.getException());
                    }
                });
    }

    // Funzione per caricare la lista degli amici
    private void loadFriends() {
        Set<String> friendsSet = new HashSet<>();

        db.collection("friends")
                .whereEqualTo("userId1", currentUser.getEmail())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<DocumentSnapshot> documents = task.getResult().getDocuments();
                        for (DocumentSnapshot document : documents) {
                            String friendEmail = document.getString("userId2");
                            friendsSet.add(friendEmail);
                        }

                        db.collection("friends")
                                .whereEqualTo("userId2", currentUser.getEmail())
                                .get()
                                .addOnCompleteListener(task2 -> {
                                    if (task2.isSuccessful()) {
                                        List<DocumentSnapshot> documents2 = task2.getResult().getDocuments();
                                        for (DocumentSnapshot document : documents2) {
                                            String friendEmail = document.getString("userId1");
                                            friendsSet.add(friendEmail);
                                        }
                                        friendsList.clear();
                                        friendsList.addAll(friendsSet);
                                        friendsAdapter.notifyDataSetChanged();
                                    } else {
                                        Log.e("Amici", "Errore nel recupero degli amici", task2.getException());
                                    }
                                });
                    } else {
                        Log.e("Amici", "Errore nel recupero degli amici", task.getException());
                    }
                });
    }

    // Funzione per accettare una richiesta di amicizia
    private void acceptRequest(String requestId) {
        db.collection("richiesteAmicizia").document(requestId).get().addOnSuccessListener(document -> {
            String from = document.getString("da");
            String to = document.getString("a");

            Map<String, Object> friendship = new HashMap<>();
            friendship.put("userIds", Arrays.asList(from, to));

            db.collection("friends").add(friendship).addOnSuccessListener(aVoid -> {
                db.collection("richiesteAmicizia").document(requestId).update("stato", "accettato");
                loadFriendRequests();
                loadFriends();
            }).addOnFailureListener(e -> Log.e("Amici", "Errore nell'aggiunta dell'amico", e));
        }).addOnFailureListener(e -> Log.e("Amici", "Errore nel recupero della richiesta di amicizia", e));
    }

    // Funzione per rifiutare una richiesta di amicizia
    private void declineRequest(String requestId) {
        db.collection("richiesteAmicizia").document(requestId).delete()
                .addOnSuccessListener(aVoid -> loadFriendRequests())
                .addOnFailureListener(e -> Log.e("Amici", "Errore nel rifiuto della richiesta di amicizia", e));
    }

    // Funzione per rimuovere un amico
    private void removeFriend(String friendEmail) {
        // Cerca tra i documenti dove l'email corrente dell'utente è presente come userId1 o userId2
        db.collection("friends")
                .whereEqualTo("userId1", currentUser.getEmail())
                .whereEqualTo("userId2", friendEmail)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        // Rimuovi il documento se la combinazione (currentUser, friendEmail) è trovata
                        for (DocumentSnapshot document : task.getResult().getDocuments()) {
                            db.collection("friends").document(document.getId()).delete()
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(Amici.this, "Amico rimosso", Toast.LENGTH_SHORT).show();
                                        loadFriends(); // Ricarica la lista degli amici
                                    })
                                    .addOnFailureListener(e -> Log.e("Amici", "Errore nella rimozione dell'amico", e));
                        }
                    } else {
                        // Cerca tra i documenti dove l'email corrente dell'utente è presente come userId2 e l'email dell'amico come userId1
                        db.collection("friends")
                                .whereEqualTo("userId2", currentUser.getEmail())
                                .whereEqualTo("userId1", friendEmail)
                                .get()
                                .addOnCompleteListener(task2 -> {
                                    if (task2.isSuccessful() && !task2.getResult().isEmpty()) {
                                        for (DocumentSnapshot document : task2.getResult().getDocuments()) {
                                            db.collection("friends").document(document.getId()).delete()
                                                    .addOnSuccessListener(aVoid -> {
                                                        Toast.makeText(Amici.this, "Amico rimosso", Toast.LENGTH_SHORT).show();
                                                        loadFriends(); // Ricarica la lista degli amici
                                                    })
                                                    .addOnFailureListener(e -> Log.e("Amici", "Errore nella rimozione dell'amico", e));
                                        }
                                    } else {
                                        Log.e("Amici", "Errore nel recupero degli amici o amico non trovato");
                                    }
                                });
                    }
                });
    }

}