package com.api.serverLaS.data;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import jakarta.json.JsonObject;

import java.util.Arrays;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class Task1Data {

    public Task1Data(String question, String stepVar, String var, String code, TraceData[] trace) {
        this.question = question;
        this.stepVar = stepVar;
        this.var = var;
        this.code = code;
        this.trace = trace;
    }

    public String question;

    public String stepVar;

    public String var;

    public String code;

    public TraceData[] trace;

    public static Task1Data fromJson(JsonObject jsonObject) {
        TraceData[] trace = Arrays.stream(jsonObject.getJsonArray("trace").toArray(new JsonObject[0])).map(jsonTraceObj -> {
            return new TraceData(jsonTraceObj.getString("text"), jsonTraceObj.getString("step"));
        }).toArray(TraceData[]::new);
        return new Task1Data(jsonObject.getString("question"), jsonObject.getString("stepVar"), jsonObject.getString("var"), jsonObject.getString("code"), trace);
    }
}
