package com.example.seiinbella;

import com.google.android.gms.maps.model.LatLng;

public interface FriendLocationListener {
    void onFriendLocationReceived(String friendEmail, LatLng location);
}