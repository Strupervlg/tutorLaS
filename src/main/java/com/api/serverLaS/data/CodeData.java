package com.api.serverLaS.data;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class CodeData {
    public CodeData(String text, String line) {
        this.text = text;
        this.line = line;
    }

    public String text;
    public String line;
}
