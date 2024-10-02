package com.api.serverLaS.requests;

public class GetNextTaskRequest {
    private String uid;

    public GetNextTaskRequest() {}

    public GetNextTaskRequest(String uid) {
        this.uid = uid;
    }


    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
