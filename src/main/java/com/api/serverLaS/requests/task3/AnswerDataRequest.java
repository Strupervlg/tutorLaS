package com.api.serverLaS.requests.task3;

public class AnswerDataRequest {

    public String var;

    public String answer;

    public AnswerDataRequest(String var, String answer) {
        this.var = var;
        this.answer = answer;
    }

    public String getVar() {
        return var;
    }

    public String getAnswer() {
        return answer;
    }
}
