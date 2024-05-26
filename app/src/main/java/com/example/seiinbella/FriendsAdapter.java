package com.example.seiinbella;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.List;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendViewHolder> {

    private List<String> friendsList;
    private Context context;
    private String currentUserEmail;
    private FirebaseFirestore db;

    public FriendsAdapter(List<String> friendsList, Context context, String currentUserEmail) {
        this.friendsList = friendsList;
        this.context = context;
        this.currentUserEmail = currentUserEmail;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.friend_item, parent, false);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        String friendEmail = friendsList.get(position);
        holder.friendEmailTextView.setText(friendEmail);
        holder.removeButton.setOnClickListener(v -> removeFriend(friendEmail, position));
    }

    @Override
    public int getItemCount() {
        return friendsList.size();
    }

    private void removeFriend(String friendEmail, int position) {
        // Remove from Firestore
        db.collection("friends")
                .whereEqualTo("userId1", currentUserEmail)
                .whereEqualTo("userId2", friendEmail)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            db.collection("friends").document(document.getId()).delete();
                        }
                    }
                });

        db.collection("friends")
                .whereEqualTo("userId2", currentUserEmail)
                .whereEqualTo("userId1", friendEmail)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            db.collection("friends").document(document.getId()).delete();
                        }
                    }
                });

        // Remove from local list and notify adapter
        friendsList.remove(position);
        notifyItemRemoved(position);
    }

    static class FriendViewHolder extends RecyclerView.ViewHolder {
        TextView friendEmailTextView;
        Button removeButton;

        FriendViewHolder(View itemView) {
            super(itemView);
            friendEmailTextView = itemView.findViewById(R.id.friend_email_text_view);
            removeButton = itemView.findViewById(R.id.removeFriendButton);
        }
    }
}