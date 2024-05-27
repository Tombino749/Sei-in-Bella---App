package com.example.seiinbella;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.seiinbella.FriendLocationListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class lista_Amici extends AppCompatActivity implements FriendLocationListener {

    private RecyclerView friendsRecyclerView;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_amici);

        friendsRecyclerView = findViewById(R.id.friends_recycler_view);
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        loadFriends();
    }

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
                                        List<String> friendsList = new ArrayList<>(friendsSet);
                                        setupFriendsRecyclerView(friendsList);
                                    } else {
                                        Log.e("FriendsListActivity", "Error getting friends", task2.getException());
                                    }
                                });
                    } else {
                        Log.e("FriendsListActivity", "Error getting friends", task.getException());
                    }
                });
    }

    private void setupFriendsRecyclerView(List<String> friendsList) {
        FriendsAdapter friendsAdapter = new FriendsAdapter(friendsList, friendEmail -> removeFriend(friendEmail, friendsList));
        friendsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        friendsRecyclerView.setAdapter(friendsAdapter);

        // Get friend locations
        getFriendLocations(friendsList);
    }

    private void removeFriend(String friendEmail, List<String> friendsList) {
        db.collection("friends")
                .whereArrayContains("userIds", currentUser.getEmail())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            List<String> userIds = (List<String>) document.get("userIds");
                            if (userIds.contains(friendEmail)) {
                                db.collection("friends").document(document.getId()).delete()
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(lista_Amici.this, "Amico rimosso", Toast.LENGTH_SHORT).show();
                                            friendsList.remove(friendEmail);
                                            setupFriendsRecyclerView(friendsList); // Reload friends list after removing
                                        })
                                        .addOnFailureListener(e -> Log.e("ListaAmici", "Errore nella rimozione dell'amico", e));
                                break;
                            }
                        }
                    } else {
                        Log.e("ListaAmici", "Errore nel recupero degli amici", task.getException());
                    }
                });
    }

    private void getFriendLocations(List<String> friendsList) {
        for (String friendEmail : friendsList) {
            db.collection("users")
                    .document(friendEmail)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            double latitude = documentSnapshot.getDouble("latitude");
                            double longitude = documentSnapshot.getDouble("longitude");
                            LatLng friendLocation = new LatLng(latitude, longitude);
                            onFriendLocationReceived(friendEmail, friendLocation);
                        }
                    })
                    .addOnFailureListener(e -> Log.e("ListaAmici", "Error getting friend location", e));
        }
    }

    public void onFriendLocationReceived(String friendEmail, LatLng location) {
        Intent intent = new Intent(this, Maps_Activity.class);
        intent.putExtra("friendEmail", friendEmail);
        intent.putExtra("friendLocation", location);
        startActivity(intent);
    }


}

