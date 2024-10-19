package com.api.serverLaS.response.task1;

public class GetHintResponse {

    String step;

    String taskInTTL;

    String hint;

    public GetHintResponse() {

    }

    public GetHintResponse(String step, String hint, String taskInTTL) {
        this.step = step;
        this.hint = hint;
        this.taskInTTL = taskInTTL;
    }

    public String getStep() {
        return step;
    }

    public String getTaskInTTL() {
        return taskInTTL;
    }

    public String getHint() {
        return hint;
    }
}
