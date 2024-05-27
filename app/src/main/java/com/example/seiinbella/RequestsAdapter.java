package com.example.seiinbella;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RequestsAdapter extends RecyclerView.Adapter<RequestsAdapter.ViewHolder> {

    private List<FriendRequest> requestsList;
    private OnRequestActionListener acceptListener;
    private OnRequestActionListener declineListener;

    public RequestsAdapter(List<FriendRequest> requestsList, OnRequestActionListener acceptListener, OnRequestActionListener declineListener) {
        this.requestsList = requestsList;
        this.acceptListener = acceptListener;
        this.declineListener = declineListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FriendRequest request = requestsList.get(position);
        holder.emailTextView.setText(request.getDa()); // Mostra l'email del mittente

        holder.acceptButton.setOnClickListener(v -> acceptListener.onRequestAction(request.getRequestId()));
        holder.declineButton.setOnClickListener(v -> declineListener.onRequestAction(request.getRequestId()));
    }

    @Override
    public int getItemCount() {
        return requestsList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView emailTextView;
        Button acceptButton;
        Button declineButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            emailTextView = itemView.findViewById(R.id.request_email);
            acceptButton = itemView.findViewById(R.id.accept_button);
            declineButton = itemView.findViewById(R.id.decline_button);
        }
    }
}
