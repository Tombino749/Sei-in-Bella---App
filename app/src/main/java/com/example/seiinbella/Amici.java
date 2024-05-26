package com.example.seiinbella;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Amici extends AppCompatActivity {

    private EditText emailInput;
    private Button addFriendButton;
    private Button viewFriendsButton;
    private RecyclerView requestsRecyclerView;
    private RecyclerView friendsRecyclerView;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_amici);

        emailInput = findViewById(R.id.email_input);
        addFriendButton = findViewById(R.id.add_friend_button);
        viewFriendsButton = findViewById(R.id.view_friends_button);
        requestsRecyclerView = findViewById(R.id.requests_recycler_view);
        friendsRecyclerView = findViewById(R.id.friends_recycler_view);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        addFriendButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            if (!TextUtils.isEmpty(email)) {
                sendFriendRequest(email);
            } else {
                Toast.makeText(Amici.this, "Enter an email", Toast.LENGTH_SHORT).show();
            }
        });

        viewFriendsButton.setOnClickListener(v -> {
            Intent intent = new Intent(Amici.this, lista_Amici.class);
            startActivity(intent);
        });

        loadFriendRequests();
    }

    private void sendFriendRequest(String email) {
        Map<String, Object> friendRequest = new HashMap<>();
        friendRequest.put("from", currentUser.getEmail());
        friendRequest.put("to", email);
        friendRequest.put("status", "pending");

        db.collection("friendRequests")
                .add(friendRequest)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(Amici.this, "Friend request sent", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(Amici.this, "Error sending friend request", Toast.LENGTH_SHORT).show();
                    Log.e("Amici", "Error sending friend request", e);
                });
    }

    private void loadFriendRequests() {
        db.collection("friendRequests")
                .whereEqualTo("to", currentUser.getEmail())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<DocumentSnapshot> documents = task.getResult().getDocuments();
                        // Populate RecyclerView with friend requests
                        // TODO: Implement RecyclerView Adapter for friend requests
                    } else {
                        Log.e("Amici", "Error getting friend requests", task.getException());
                    }
                });
    }
}
