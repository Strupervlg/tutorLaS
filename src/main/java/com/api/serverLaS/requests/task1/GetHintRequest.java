package com.api.serverLaS.requests.task1;

public class GetHintRequest {

    private String uid;

    private String[] steps;

    private String taskInTTL;

    private String stepVar;

    private String var;

    private int taskId;

    public GetHintRequest() {}

    public GetHintRequest(String uid, String[] steps, String taskInTTL, String stepVar, String var, int taskId) {
        this.uid = uid;
        this.steps = steps;
        this.taskInTTL = taskInTTL;
        this.stepVar = stepVar;
        this.var = var;
        this.taskId = taskId;
    }

    public String getUid() {
        return uid;
    }

    public String[] getSteps() {
        return steps;
    }

    public String getTaskInTTL() {
        return taskInTTL;
    }


    public int getTaskId() {
        return taskId;
    }

    public String getStepVar() {
        return stepVar;
    }

    public String getVar() {
        return var;
    }
}
