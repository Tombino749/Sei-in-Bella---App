package com.example.seiinbella;

public class FriendRequest {
    private String requestId;
    private String da; // Chi invia la richiesta
    private String a; // Chi riceve la richiesta
    private String stato;

    // Costruttore vuoto richiesto da Firebase
    public FriendRequest() {
    }

    // Getter e setter
    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getDa() {
        return da;
    }

    public void setDa(String da) {
        this.da = da;
    }

    public String getA() {
        return a;
    }

    public void setA(String a) {
        this.a = a;
    }

    public String getStato() {
        return stato;
    }

    public void setStato(String stato) {
        this.stato = stato;
    }
}
