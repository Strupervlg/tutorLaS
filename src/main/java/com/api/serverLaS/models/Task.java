package com.api.serverLaS.models;

public class Task {

    private int id;

    private String name;

    private String nameTtl;

    private String nameJson;

    public Task(int id, String name, String nameTtl, String nameJson) {
        this.id = id;
        this.name = name;
        this.nameTtl = nameTtl;
        this.nameJson = nameJson;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNameTtl() {
        return nameTtl;
    }

    public void setNameTtl(String nameTtl) {
        this.nameTtl = nameTtl;
    }

    public String getNameJson() {
        return nameJson;
    }

    public void setNameJson(String nameJson) {
        this.nameJson = nameJson;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
