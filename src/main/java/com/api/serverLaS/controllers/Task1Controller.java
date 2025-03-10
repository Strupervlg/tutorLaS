package com.api.serverLaS.controllers;

import com.api.serverLaS.exceptions.NotFoundException;
import com.api.serverLaS.requests.task1.CheckAnswerRequest;
import com.api.serverLaS.requests.task1.CompleteTaskRequest;
import com.api.serverLaS.requests.task1.GetHintRequest;
import com.api.serverLaS.requests.GetNextTaskRequest;
import com.api.serverLaS.response.CheckAnswerResponse;
import com.api.serverLaS.response.CompleteTaskResponse;
import com.api.serverLaS.response.task1.GetHintResponse;
import com.api.serverLaS.response.GetNextTaskResponse;
import com.api.serverLaS.services.Task1Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@CrossOrigin(origins = "*")
public class Task1Controller {

    @Autowired
    public Task1Service task1Service;

    @PostMapping("/task-1/check-answer")
    public CheckAnswerResponse checkAnswer(@RequestBody CheckAnswerRequest checkAnswerRequest) {
        return task1Service.checkAnswer(checkAnswerRequest);
    }

    @PostMapping("/task-1/complete-task")
    public CompleteTaskResponse completeTask(@RequestBody CompleteTaskRequest completeTaskRequest) {
        if(completeTaskRequest.getTaskId() == 0) {
            throw new NotFoundException("Task not found");
        }
        return task1Service.completeTask(completeTaskRequest);
    }

    @PostMapping("/task-1/get-hint")
    public GetHintResponse getHint(@RequestBody GetHintRequest getHintRequest) {
        return task1Service.getHint(getHintRequest);
    }

    @PostMapping("/task-1/get-next")
    public GetNextTaskResponse getNext(@RequestBody GetNextTaskRequest getNextTaskRequest) {
        return task1Service.getNext(getNextTaskRequest);
    }

    @PostMapping("/en/task-1/get-next")
    public GetNextTaskResponse getEnNext(@RequestBody GetNextTaskRequest getNextTaskRequest) {
        return task1Service.getEnNext(getNextTaskRequest);
    }
}
