package com.api.serverLaS.requests;

public class AuthRequest {
    private String fio;

    private String group;

    public AuthRequest() {}

    public AuthRequest(String fio, String group) {
        this.fio = fio;
        this.group = group;
    }

    public String getFio() {
        return fio;
    }

    public String getGroup() {
        return group;
    }
}
