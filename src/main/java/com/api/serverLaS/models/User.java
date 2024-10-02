package com.api.serverLaS.models;

public class User {
    private String uid;

    private String fio;

    private String groupName;

    public User(String uid, String fio, String groupName) {
        this.uid = uid;
        this.fio = fio;
        this.groupName = groupName;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getFio() {
        return fio;
    }

    public void setFio(String fio) {
        this.fio = fio;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}
