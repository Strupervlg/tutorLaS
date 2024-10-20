package com.api.serverLaS.requests.task3;

public class CheckAnswerRequest {

    private String uid;

    private AnswerDataRequest[] answers;

    private String taskInTTL;

    private int taskId;

    public CheckAnswerRequest() {}

    public CheckAnswerRequest(String uid, AnswerDataRequest[] answers, String taskInTTL, int taskId) {
        this.uid = uid;
        this.answers = answers;
        this.taskInTTL = taskInTTL;
        this.taskId = taskId;
    }

    public String getUid() {
        return uid;
    }

    public AnswerDataRequest[] getAnswers() {
        return answers;
    }

    public String getTaskInTTL() {
        return taskInTTL;
    }

    public int getTaskId() {
        return taskId;
    }
}
