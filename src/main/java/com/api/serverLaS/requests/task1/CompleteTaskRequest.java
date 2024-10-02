package com.api.serverLaS.requests.task1;

public class CompleteTaskRequest {

    private String uid;

    private String taskInTTL;

    private String stepVar;

    private String var;

    private int taskId;

    public CompleteTaskRequest() {}

    public CompleteTaskRequest(String uid, String taskInTTL, String stepVar, String var, int taskId) {
        this.uid = uid;
        this.taskInTTL = taskInTTL;
        this.stepVar = stepVar;
        this.var = var;
        this.taskId = taskId;
    }

    public String getUid() {
        return uid;
    }

    public String getTaskInTTL() {
        return taskInTTL;
    }

    public String getStepVar() {
        return stepVar;
    }

    public String getVar() {
        return var;
    }

    public int getTaskId() {
        return taskId;
    }
}
