package com.api.serverLaS.data;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class TraceData {

    public TraceData(String text, String step) {
        this.text = text;
        this.step = step;
    }

    public String text;
    public String step;
}
