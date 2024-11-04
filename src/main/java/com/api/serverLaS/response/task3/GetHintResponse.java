package com.api.serverLaS.response.task3;

public class GetHintResponse {

    String hint;

    String taskInTTL;

    public GetHintResponse() {

    }

    public GetHintResponse(String hint, String taskInTTL) {
        this.hint = hint;
        this.taskInTTL = taskInTTL;
    }

    public String getHint() {
        return hint;
    }

    public String getTaskInTTL() {
        return taskInTTL;
    }
}
