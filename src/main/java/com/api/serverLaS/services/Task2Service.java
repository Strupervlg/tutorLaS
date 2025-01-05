package com.api.serverLaS.services;

import com.api.serverLaS.data.NextTaskData;
import com.api.serverLaS.data.Task2Data;
import com.api.serverLaS.repositories.SolutionRepository;
import com.api.serverLaS.requests.GetNextTaskRequest;
import com.api.serverLaS.requests.task2.CheckAnswerRequest;
import com.api.serverLaS.requests.task2.CompleteTaskRequest;
import com.api.serverLaS.requests.task2.GetHintRequest;
import com.api.serverLaS.response.CheckAnswerResponse;
import com.api.serverLaS.response.CompleteTaskResponse;
import com.api.serverLaS.response.GetNextTaskResponse;
import com.api.serverLaS.response.task2.GetHintResponse;
import its.model.DomainSolvingModel;
import its.model.definition.DomainModel;
import its.model.definition.ObjectRef;
import its.model.definition.rdf.DomainRDFFiller;
import its.model.definition.rdf.DomainRDFWriter;
import its.model.nodes.BranchResultNode;
import its.reasoner.BranchResultProcessor;
import its.reasoner.LearningSituation;
import its.reasoner.nodes.DecisionTreeEvaluationResult;
import its.reasoner.nodes.DecisionTreeReasoner;
import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.ModelFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringWriter;

import java.util.*;

@Service
public class Task2Service {

    @Autowired
    private SolutionRepository solutionRepository;

    @Autowired
    public CommonTaskService commonTaskService;

    public DomainSolvingModel model = new DomainSolvingModel(
            this.getClass().getClassLoader().getResource("Task2/"),
            DomainSolvingModel.BuildMethod.DICT_RDF
    );

    public CheckAnswerResponse checkAnswer(CheckAnswerRequest request) {
        DomainModel situationDomain = this.model.getDomainModel().copy();
        try {
            DomainRDFFiller.fillDomain(situationDomain,
                    ModelFactory.createDefaultModel().read(IOUtils.toInputStream(request.getTaskInTTL(), "UTF-8"), null, "TTL"),
                    Set.of(DomainRDFFiller.Option.NARY_RELATIONSHIPS_OLD_COMPAT),
                    null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        situationDomain.validateAndThrow();

        LearningSituation situation = new LearningSituation(situationDomain,
                new HashMap<>(Map.of(
                        "usageLine", new ObjectRef(request.getUsageLine()),
                        "var", new ObjectRef(request.getVar()),
                        "prefix", new ObjectRef(request.getPrefix())
                ))
        );

        BranchResultProcessor resultProcessor = new BranchResultProcessor();
        DecisionTreeReasoner.solve(model.getDecisionTree(), situation, resultProcessor);
        String errorText = commonTaskService.generateErrorText(resultProcessor.getList(), situationDomain, request.getUid(), request.getTaskId(), request.getUsageLine());
        String correctText = commonTaskService.generateHintText(resultProcessor.getList(), situationDomain);
        commonTaskService.addCountOfCorrectToDB(errorText, request.getUid(), request.getTaskId(), request.getUsageLine(), correctText);

        StringWriter stringWriter = new StringWriter();
        DomainRDFWriter.saveDomain(situationDomain, stringWriter, "poas:poas/", Set.of(DomainRDFWriter.Option.NARY_RELATIONSHIPS_OLD_COMPAT));
        return new CheckAnswerResponse(errorText.isEmpty(), errorText, stringWriter.toString());
    }

    public CompleteTaskResponse completeTask(CompleteTaskRequest request) {
        DomainModel situationDomain = this.model.getDomainModel().copy();
        try {
            DomainRDFFiller.fillDomain(situationDomain,
                    ModelFactory.createDefaultModel().read(IOUtils.toInputStream(request.getTaskInTTL(), "UTF-8"), null, "TTL"),
                    Set.of(DomainRDFFiller.Option.NARY_RELATIONSHIPS_OLD_COMPAT),
                    null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        situationDomain.validateAndThrow();

        LearningSituation situation = new LearningSituation(situationDomain,
                new HashMap<>(Map.of(
                        "var", new ObjectRef(request.getVar()),
                        "prefix", new ObjectRef(request.getPrefix())
                ))
        );

        BranchResultProcessor resultProcessor = new BranchResultProcessor();
        DecisionTreeReasoner.solve(model.getDecisionTrees().get("all"), situation, resultProcessor);
        List<DecisionTreeEvaluationResult<BranchResultNode>> branchResultNodes = resultProcessor.getList();
        branchResultNodes.sort(
                Comparator.comparingInt(
                        (DecisionTreeEvaluationResult<BranchResultNode> node) -> {
                            if(node.getVariablesSnapshot().get("step") != null) {
                                return (int) node.getVariablesSnapshot().get("step").findIn(situationDomain).getPropertyValue("number");
                            } else {
                                return 0;
                            }}));
        String errorText = commonTaskService.generateErrorText(branchResultNodes, situationDomain, request.getUid(), request.getTaskId(), "all");
        String[] lines = commonTaskService.getErrorLines(branchResultNodes, situationDomain, "line");

        return new CompleteTaskResponse(errorText.isEmpty(), errorText, lines);
    }

    public GetHintResponse getHint(GetHintRequest request) {
        DomainModel situationDomain = this.model.getDomainModel().copy();
        try {
            DomainRDFFiller.fillDomain(situationDomain,
                    ModelFactory.createDefaultModel().read(IOUtils.toInputStream(request.getTaskInTTL(), "UTF-8"), null, "TTL"),
                    Set.of(DomainRDFFiller.Option.NARY_RELATIONSHIPS_OLD_COMPAT),
                    null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        situationDomain.validateAndThrow();
        String correctLine = "";
        String hintText = "";
        for(String line : request.getLines()) {
            LearningSituation situation = new LearningSituation(situationDomain,
                    new HashMap<>(Map.of(
                            "usageLine", new ObjectRef(line),
                            "var", new ObjectRef(request.getVar()),
                            "prefix", new ObjectRef(request.getPrefix())
                    ))
            );
            BranchResultProcessor resultProcessor = new BranchResultProcessor();
            DecisionTreeReasoner.solve(model.getDecisionTree(), situation, resultProcessor);
            hintText = commonTaskService.generateHintText(resultProcessor.getList(), situationDomain);
            if(!hintText.isEmpty()) {
                correctLine = line;
                break;
            }
        }

        commonTaskService.addCountOfHintsToDB(correctLine, request.getUid(), request.getTaskId(), hintText);

        StringWriter stringWriter = new StringWriter();
        DomainRDFWriter.saveDomain(situationDomain, stringWriter, "poas:poas/", Set.of(DomainRDFWriter.Option.NARY_RELATIONSHIPS_OLD_COMPAT));
        return new GetHintResponse(correctLine, hintText, stringWriter.toString());
    }

    public GetNextTaskResponse getNext(GetNextTaskRequest getNextTaskRequest) {
        NextTaskData data = commonTaskService.getNext(getNextTaskRequest, 2);
        if(data.getTaskId() == -1) {
            data = commonTaskService.getNext(getNextTaskRequest, 21);
        }

        return new GetNextTaskResponse(data.getTaskId(), data.getTaskInTTL(), data.getTask() != null ? Task2Data.fromJson(data.getTask()) : data.getTask());
    }
}
