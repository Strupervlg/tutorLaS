package com.api.serverLaS.data;

import jakarta.json.JsonObject;

public class NextTaskData {
    int taskId;

    String taskInTTL;

    JsonObject task;

    public NextTaskData(int taskId, String taskInTTL, JsonObject task) {
        this.taskId = taskId;
        this.taskInTTL = taskInTTL;
        this.task = task;
    }

    public int getTaskId() {
        return taskId;
    }

    public JsonObject getTask() {
        return task;
    }

    public String getTaskInTTL() {
        return taskInTTL;
    }
}
