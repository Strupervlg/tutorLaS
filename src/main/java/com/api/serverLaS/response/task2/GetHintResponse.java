package com.api.serverLaS.response.task2;

public class GetHintResponse {

    String line;

    String taskInTTL;

    String hint;

    public GetHintResponse() {

    }

    public GetHintResponse(String line, String hint, String taskInTTL) {
        this.line = line;
        this.hint = hint;
        this.taskInTTL = taskInTTL;
    }

    public String getLine() {
        return line;
    }

    public String getTaskInTTL() {
        return taskInTTL;
    }

    public String getHint() {
        return hint;
    }
}
