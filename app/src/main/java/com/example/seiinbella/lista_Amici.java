package com.example.seiinbella;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class lista_Amici extends AppCompatActivity {

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
        FriendsAdapter friendsAdapter = new FriendsAdapter(friendsList, this, currentUser.getEmail());
        friendsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        friendsRecyclerView.setAdapter(friendsAdapter);
    }
}
