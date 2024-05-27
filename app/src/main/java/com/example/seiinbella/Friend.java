package com.example.seiinbella;

public class Friend {
    private String email;

    public Friend() {
        // Costruttore vuoto richiesto da Firestore
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
