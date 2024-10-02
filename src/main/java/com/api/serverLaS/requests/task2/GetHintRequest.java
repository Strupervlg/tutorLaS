package com.api.serverLaS.requests.task2;

public class GetHintRequest {

    private String uid;

    private String[] lines;

    private String taskInTTL;

    private String prefix;

    private String var;

    private int taskId;

    public GetHintRequest() {}

    public GetHintRequest(String uid, String[] lines, String taskInTTL, String prefix, String var, int taskId) {
        this.uid = uid;
        this.lines = lines;
        this.taskInTTL = taskInTTL;
        this.prefix = prefix;
        this.var = var;
        this.taskId = taskId;
    }

    public String getUid() {
        return uid;
    }

    public String[] getLines() {
        return lines;
    }

    public String getTaskInTTL() {
        return taskInTTL;
    }


    public int getTaskId() {
        return taskId;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getVar() {
        return var;
    }
}
