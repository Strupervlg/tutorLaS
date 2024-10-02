package com.api.serverLaS.response;

public class GetNextTaskResponse {

    int taskId;

    String taskInTTL;

    Object task;

    public GetNextTaskResponse(int taskId, String taskInTTL, Object task) {
        this.taskId = taskId;
        this.taskInTTL = taskInTTL;
        this.task = task;
    }

    public int getTaskId() {
        return taskId;
    }

    public Object getTask() {
        return task;
    }

    public String getTaskInTTL() {
        return taskInTTL;
    }
}
