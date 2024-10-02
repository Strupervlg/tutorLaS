package com.api.serverLaS.data;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import jakarta.json.JsonObject;

import java.util.Arrays;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class Task2Data {

    public Task2Data(String question, String prefix, String var, CodeData[] code) {
        this.question = question;
        this.prefix = prefix;
        this.var = var;
        this.code = code;
    }

    public String question;

    public String prefix;

    public String var;

    public CodeData[] code;

    public static Task2Data fromJson(JsonObject jsonObject) {
        CodeData[] code = Arrays.stream(jsonObject.getJsonArray("code").toArray(new JsonObject[0])).map(jsonTraceObj -> {
            return new CodeData(jsonTraceObj.getString("text"), jsonTraceObj.getString("line"));
        }).toArray(CodeData[]::new);
        return new Task2Data(jsonObject.getString("question"), jsonObject.getString("prefix"), jsonObject.getString("var"), code);
    }
}
