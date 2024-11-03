package com.api.serverLaS.response;

public class CompleteTaskResponse {

    boolean result;

    String errorText;

    String[] lines;

    public CompleteTaskResponse() {

    }

    public CompleteTaskResponse(boolean result, String errorText, String[] lines) {
        this.result = result;
        this.errorText = errorText;
        this.lines = lines;
    }

    public boolean getResult() {
        return result;
    }

    public String getErrorText() {
        return errorText;
    }

    public String[] getLines() {
        return lines;
    }
}
