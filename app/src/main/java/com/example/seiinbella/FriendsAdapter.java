package com.example.seiinbella;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.ViewHolder> {

    private List<String> friendsList;
    private OnFriendActionListener removeListener;

    public FriendsAdapter(List<String> friendsList, OnFriendActionListener removeListener) {
        this.friendsList = friendsList;
        this.removeListener = removeListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String friendEmail = friendsList.get(position);
        holder.emailTextView.setText(friendEmail);

        holder.removeButton.setOnClickListener(v -> removeListener.onFriendAction(friendEmail));
    }

    @Override
    public int getItemCount() {
        return friendsList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView emailTextView;
        Button removeButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            emailTextView = itemView.findViewById(R.id.friend_email);
            removeButton = itemView.findViewById(R.id.remove_button);
        }
    }

    public interface OnFriendActionListener {
        void onFriendAction(String friendEmail);
    }
}
