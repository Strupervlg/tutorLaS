package com.api.serverLaS.controllers;

import com.api.serverLaS.exceptions.NotFoundException;
import com.api.serverLaS.requests.task2.CheckAnswerRequest;
import com.api.serverLaS.requests.task2.CompleteTaskRequest;
import com.api.serverLaS.requests.task2.GetHintRequest;
import com.api.serverLaS.requests.GetNextTaskRequest;
import com.api.serverLaS.response.CheckAnswerResponse;
import com.api.serverLaS.response.CompleteTaskResponse;
import com.api.serverLaS.response.task2.GetHintResponse;
import com.api.serverLaS.response.GetNextTaskResponse;
import com.api.serverLaS.services.Task2Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
public class Task2Controller {

    @Autowired
    public Task2Service task2Service;

    @PostMapping("/task-2/check-answer")
    public CheckAnswerResponse checkAnswer(@RequestBody CheckAnswerRequest checkAnswerRequest) {
        return task2Service.checkAnswer(checkAnswerRequest);
    }

    @PostMapping("/task-2/complete-task")
    public CompleteTaskResponse completeTask(@RequestBody CompleteTaskRequest completeTaskRequest) {
        if(completeTaskRequest.getTaskId() == 0) {
            throw new NotFoundException("Task not found");
        }
        return task2Service.completeTask(completeTaskRequest);
    }

    @PostMapping("/task-2/get-hint")
    public GetHintResponse getHint(@RequestBody GetHintRequest getHintRequest) {
        return task2Service.getHint(getHintRequest);
    }

    @PostMapping("/task-2/get-next")
    public GetNextTaskResponse getNext(@RequestBody GetNextTaskRequest getNextTaskRequest) {
        return task2Service.getNext(getNextTaskRequest);
    }

    @PostMapping("/en/task-2/get-next")
    public GetNextTaskResponse getEnNext(@RequestBody GetNextTaskRequest getNextTaskRequest) {
        return task2Service.getEnNext(getNextTaskRequest);
    }
}
