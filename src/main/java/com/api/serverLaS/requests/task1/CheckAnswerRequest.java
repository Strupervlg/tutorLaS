package com.api.serverLaS.requests.task1;

public class CheckAnswerRequest {

    private String uid;

    private String step;

    private String stepVar;

    private String var;

    private String taskInTTL;

    private int taskId;

    public CheckAnswerRequest() {}

    public CheckAnswerRequest(String uid, String step, String taskInTTL, String stepVar, String var, int taskId) {
        this.uid = uid;
        this.step = step;
        this.taskInTTL = taskInTTL;
        this.stepVar = stepVar;
        this.var = var;
        this.taskId = taskId;
    }

    public String getUid() {
        return uid;
    }

    public String getStep() {
        return step;
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
