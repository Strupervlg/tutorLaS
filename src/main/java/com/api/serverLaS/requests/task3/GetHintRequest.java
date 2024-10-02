package com.api.serverLaS.requests.task3;

public class GetHintRequest {

    private String uid;

    private String[] answers;

    private String taskInTTL;

    private String var;

    private int taskId;

    public GetHintRequest() {}

    public GetHintRequest(String uid, String[] answers, String taskInTTL, String var, int taskId) {
        this.uid = uid;
        this.answers = answers;
        this.taskInTTL = taskInTTL;
        this.var = var;
        this.taskId = taskId;
    }

    public String getUid() {
        return uid;
    }

    public String[] getAnswers() {
        return answers;
    }

    public String getTaskInTTL() {
        return taskInTTL;
    }


    public int getTaskId() {
        return taskId;
    }

    public String getVar() {
        return var;
    }
}
