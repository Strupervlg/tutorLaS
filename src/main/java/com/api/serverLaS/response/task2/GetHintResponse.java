package com.api.serverLaS.response.task2;

public class GetHintResponse {

    String line;

    String taskInTTL;

    public GetHintResponse() {

    }

    public GetHintResponse(String line, String taskInTTL) {
        this.line = line;
        this.taskInTTL = taskInTTL;
    }

    public String getLine() {
        return line;
    }

    public String getTaskInTTL() {
        return taskInTTL;
    }
}
