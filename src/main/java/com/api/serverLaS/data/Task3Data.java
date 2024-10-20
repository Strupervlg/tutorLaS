package com.api.serverLaS.data;

import jakarta.json.JsonObject;

import java.util.Arrays;

public class Task3Data {
    public Task3Data(String question, VariableData[] vars, String expression, String functionDef) {
        this.question = question;
        this.vars = vars;
        this.expression = expression;
        this.function_def = functionDef;
    }

    public String question;

    public VariableData[] vars;

    public String expression;

    public String function_def;

    public static Task3Data fromJson(JsonObject jsonObject) {
        VariableData[] vars = Arrays.stream(jsonObject.getJsonArray("vars").toArray(new JsonObject[0])).map(jsonTraceObj -> {
            return new VariableData(jsonTraceObj.getString("name"), jsonTraceObj.getString("object"));
        }).toArray(VariableData[]::new);
        return new Task3Data(jsonObject.getString("question"), vars, jsonObject.getString("expression"), jsonObject.getString("function_def"));
    }
}
