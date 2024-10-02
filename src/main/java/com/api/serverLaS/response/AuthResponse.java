package com.api.serverLaS.response;

public class AuthResponse {

    String uid;

    public AuthResponse(String uid) {
        this.uid = uid;
    }

    public String getUid() {
        return uid;
    }
}
