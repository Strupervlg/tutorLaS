package com.api.serverLaS.data;

import jakarta.json.JsonObject;

import java.util.Arrays;

public class Task3Data {
    public Task3Data(String question, String var, String expression, String functionDef) {
        this.question = question;
        this.var = var;
        this.expression = expression;
        this.function_def = functionDef;
    }

    public String question;

    public String var;

    public String expression;

    public String function_def;

    public static Task3Data fromJson(JsonObject jsonObject) {
        return new Task3Data(jsonObject.getString("question"), jsonObject.getString("var"), jsonObject.getString("expression"), jsonObject.getString("function_def"));
    }
}
