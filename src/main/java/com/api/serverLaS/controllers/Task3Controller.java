package com.api.serverLaS.controllers;

import com.api.serverLaS.requests.GetNextTaskRequest;
import com.api.serverLaS.requests.task3.CheckAnswerRequest;
import com.api.serverLaS.requests.task3.GetHintRequest;
import com.api.serverLaS.response.CheckAnswerResponse;
import com.api.serverLaS.response.GetNextTaskResponse;
import com.api.serverLaS.response.task3.GetHintResponse;
import com.api.serverLaS.services.ErrorMessageService;
import com.api.serverLaS.services.Task3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
public class Task3Controller {

    @Autowired
    public Task3Service task3Service;

    @Autowired
    public ErrorMessageService errorMessageService;

    @PostMapping("/task-3/check-answer")
    public CheckAnswerResponse checkAnswer(@RequestBody CheckAnswerRequest checkAnswerRequest) {
        return task3Service.checkAnswer(checkAnswerRequest);
    }

//    @PostMapping("/task-3/get-hint")
//    public GetHintResponse getHint(@RequestBody GetHintRequest getHintRequest) {
//        return task3Service.getHint(getHintRequest);
//    }

    @PostMapping("/task-3/get-next")
    public GetNextTaskResponse getNext(@RequestBody GetNextTaskRequest getNextTaskRequest) {
        return task3Service.getNext(getNextTaskRequest);
    }
}
