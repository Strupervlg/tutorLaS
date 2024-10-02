package com.api.serverLaS.response;

public class CompleteTaskResponse {

    boolean result;

    String errorText;

    public CompleteTaskResponse() {

    }

    public CompleteTaskResponse(boolean result, String errorText) {
        this.result = result;
        this.errorText = errorText;
    }

    public boolean getResult() {
        return result;
    }

    public String getErrorText() {
        return errorText;
    }
}
