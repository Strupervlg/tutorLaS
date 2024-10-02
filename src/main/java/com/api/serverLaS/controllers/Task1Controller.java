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
import com.api.serverLaS.services.ErrorMessageService;
import com.api.serverLaS.services.Task1Service;
import its.model.DomainSolvingModel;
import its.model.definition.Domain;
import its.model.definition.ObjectRef;
import its.model.definition.rdf.DomainRDFFiller;
import its.reasoner.LearningSituation;
import its.reasoner.nodes.DecisionTreeReasoner;
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

    @Autowired
    public ErrorMessageService errorMessageService;

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
        return task1Service.getNext(getNextTaskRequest, 1);
    }



    @PostMapping("/test")
    public String test() {
        DomainSolvingModel model = new DomainSolvingModel(
                this.getClass().getClassLoader().getResource("Task1/"),
                DomainSolvingModel.BuildMethod.DICT_RDF
        );
        Domain situationDomain = model.getDomain().copy();
        DomainRDFFiller.fillDomain(situationDomain,
                this.getClass().getClassLoader().getResource("tasks/task1/11.ttl").getPath(),
                Set.of(DomainRDFFiller.Option.NARY_RELATIONSHIPS_OLD_COMPAT),
                null);

        situationDomain.validateAndThrow();

        LearningSituation situation = new LearningSituation(situationDomain,
                new HashMap<>(Map.of(
                        "stepVar", new ObjectRef("step2"),
                        "var", new ObjectRef("VariableA")
                ))
        );

        List<DecisionTreeReasoner.DecisionTreeEvaluationResult> branchResultNodes = DecisionTreeReasoner.solve(model.getDecisionTrees().get("all"), situation);
        String errorText = "";
//        Collections.reverse(branchResultNodes); //TODO возможно можно убрать это
        for(DecisionTreeReasoner.DecisionTreeEvaluationResult branchResultNode : branchResultNodes) {
            if(!branchResultNode.getNode().getValue() && branchResultNode.getNode().getMetadata().get("alias") != null) {
                //TODO сделать подстановку параметров в ошибках
                errorText += errorMessageService.generateMessage(branchResultNode.getNode().getMetadata().get("alias").toString(), branchResultNode.getVariablesSnapshot(), situationDomain) + "<br>";

            }
        }
        return errorText;
    }
}
