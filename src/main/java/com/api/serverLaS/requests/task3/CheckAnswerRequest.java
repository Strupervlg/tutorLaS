package com.api.serverLaS.requests.task3;

public class CheckAnswerRequest {

    private String uid;

    private String answer;

    private String var;

    private String taskInTTL;

    private int taskId;

    public CheckAnswerRequest() {}

    public CheckAnswerRequest(String uid, String answer, String taskInTTL, String var, int taskId) {
        this.uid = uid;
        this.answer = answer;
        this.taskInTTL = taskInTTL;
        this.var = var;
        this.taskId = taskId;
    }

    public String getUid() {
        return uid;
    }

    public String getAnswer() {
        return answer;
    }

    public String getTaskInTTL() {
        return taskInTTL;
    }

    public String getVar() {
        return var;
    }

    public int getTaskId() {
        return taskId;
    }
}
