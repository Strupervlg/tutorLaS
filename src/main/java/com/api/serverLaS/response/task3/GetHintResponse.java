package com.api.serverLaS.response.task3;

public class GetHintResponse {

    String step;

    String taskInTTL;

    public GetHintResponse() {

    }

    public GetHintResponse(String step, String taskInTTL) {
        this.step = step;
        this.taskInTTL = taskInTTL;
    }

    public String getStep() {
        return step;
    }

    public String getTaskInTTL() {
        return taskInTTL;
    }
}
