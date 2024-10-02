package com.api.serverLaS.response;

public class CheckAnswerResponse {

    boolean result;

    String errorText;

    String taskInTTL;

    public CheckAnswerResponse() {

    }

    public CheckAnswerResponse(boolean result, String errorText, String taskInTTL) {
        this.result = result;
        this.errorText = errorText;
        this.taskInTTL = taskInTTL;
    }

    public boolean getResult() {
        return result;
    }

    public String getErrorText() {
        return errorText;
    }

    public String getTaskInTTL() {
        return taskInTTL;
    }
}
