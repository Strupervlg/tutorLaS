package com.api.serverLaS.requests.task2;

public class CompleteTaskRequest {

    private String uid;

    private String taskInTTL;

    private String prefix;

    private String var;

    private int taskId;

    public CompleteTaskRequest() {}

    public CompleteTaskRequest(String uid, String taskInTTL, String prefix, String var, int taskId) {
        this.uid = uid;
        this.taskInTTL = taskInTTL;
        this.prefix = prefix;
        this.var = var;
        this.taskId = taskId;
    }

    public String getUid() {
        return uid;
    }

    public String getTaskInTTL() {
        return taskInTTL;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getVar() {
        return var;
    }

    public int getTaskId() {
        return taskId;
    }
}
