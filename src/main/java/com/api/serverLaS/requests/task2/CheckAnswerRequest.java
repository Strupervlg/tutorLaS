package com.api.serverLaS.requests.task2;

public class CheckAnswerRequest {

    private String uid;

    private String usageLine;

    private String prefix;

    private String var;

    private String taskInTTL;

    private int taskId;

    public CheckAnswerRequest() {}

    public CheckAnswerRequest(String uid, String usageLine, String taskInTTL, String prefix, String var, int taskId) {
        this.uid = uid;
        this.usageLine = usageLine;
        this.taskInTTL = taskInTTL;
        this.prefix = prefix;
        this.var = var;
        this.taskId = taskId;
    }

    public String getUid() {
        return uid;
    }

    public String getUsageLine() {
        return usageLine;
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
