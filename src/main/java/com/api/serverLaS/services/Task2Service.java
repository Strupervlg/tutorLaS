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
import its.model.definition.Domain;
import its.model.definition.ObjectRef;
import its.model.definition.rdf.DomainRDFFiller;
import its.model.definition.rdf.DomainRDFWriter;
import its.reasoner.LearningSituation;
import its.reasoner.nodes.DecisionTreeReasoner;
import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.ModelFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        Domain situationDomain = this.model.getDomain().copy();
        DomainRDFFiller.fillDomain(situationDomain,
                ModelFactory.createDefaultModel().read(IOUtils.toInputStream(request.getTaskInTTL(), "UTF-8"), null, "TTL"),
                Set.of(DomainRDFFiller.Option.NARY_RELATIONSHIPS_OLD_COMPAT),
                null);

        situationDomain.validateAndThrow();

        LearningSituation situation = new LearningSituation(situationDomain,
                new HashMap<>(Map.of(
                        "usageLine", new ObjectRef(request.getUsageLine()),
                        "var", new ObjectRef(request.getVar()),
                        "prefix", new ObjectRef(request.getPrefix())
                ))
        );

        List<DecisionTreeReasoner.DecisionTreeEvaluationResult> branchResultNodes = DecisionTreeReasoner.solve(model.getDecisionTree(), situation);
        String errorText = commonTaskService.generateErrorText(branchResultNodes, situationDomain, request.getUid(), request.getTaskId());

        StringWriter stringWriter = new StringWriter();
        DomainRDFWriter.saveDomain(situationDomain, stringWriter, "poas:poas/", Set.of(DomainRDFWriter.Option.NARY_RELATIONSHIPS_OLD_COMPAT));
        return new CheckAnswerResponse(errorText.isEmpty(), errorText, stringWriter.toString());
    }

    public CompleteTaskResponse completeTask(CompleteTaskRequest request) {
        Domain situationDomain = this.model.getDomain().copy();
        DomainRDFFiller.fillDomain(situationDomain,
                ModelFactory.createDefaultModel().read(IOUtils.toInputStream(request.getTaskInTTL(), "UTF-8"), null, "TTL"),
                Set.of(DomainRDFFiller.Option.NARY_RELATIONSHIPS_OLD_COMPAT),
                null);

        situationDomain.validateAndThrow();

        LearningSituation situation = new LearningSituation(situationDomain,
                new HashMap<>(Map.of(
                        "var", new ObjectRef(request.getVar()),
                        "prefix", new ObjectRef(request.getPrefix())
                ))
        );

        List<DecisionTreeReasoner.DecisionTreeEvaluationResult> branchResultNodes = DecisionTreeReasoner.solve(model.getDecisionTrees().get("all"), situation);
        branchResultNodes.sort(
                Comparator.comparingInt(
                        (DecisionTreeReasoner.DecisionTreeEvaluationResult node) -> {
                            if(node.getVariablesSnapshot().get("step") != null) {
                                return (int) node.getVariablesSnapshot().get("step").findIn(situationDomain).getPropertyValue("number");
                            } else {
                                return 0;
                            }}));
        String errorText = commonTaskService.generateErrorText(branchResultNodes, situationDomain, request.getUid(), request.getTaskId());

        return new CompleteTaskResponse(errorText.isEmpty(), errorText);
    }

    public GetHintResponse getHint(GetHintRequest request) {
        Domain situationDomain = this.model.getDomain().copy();
        DomainRDFFiller.fillDomain(situationDomain,
                ModelFactory.createDefaultModel().read(IOUtils.toInputStream(request.getTaskInTTL(), "UTF-8"), null, "TTL"),
                Set.of(DomainRDFFiller.Option.NARY_RELATIONSHIPS_OLD_COMPAT),
                null);

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
            List<DecisionTreeReasoner.DecisionTreeEvaluationResult> branchResultNodes = DecisionTreeReasoner.solve(model.getDecisionTree(), situation);
            hintText = commonTaskService.generateHintText(branchResultNodes, situationDomain);
            if(!hintText.isEmpty()) {
                correctLine = line;
                break;
            }
        }

        commonTaskService.addCountOfHintsToDB(correctLine, request.getUid(), request.getTaskId());

        StringWriter stringWriter = new StringWriter();
        DomainRDFWriter.saveDomain(situationDomain, stringWriter, "poas:poas/", Set.of(DomainRDFWriter.Option.NARY_RELATIONSHIPS_OLD_COMPAT));
        return new GetHintResponse(correctLine, hintText, stringWriter.toString());
    }

    public GetNextTaskResponse getNext(GetNextTaskRequest getNextTaskRequest) {
        NextTaskData data = commonTaskService.getNext(getNextTaskRequest, 2);

        return new GetNextTaskResponse(data.getTaskId(), data.getTaskInTTL(), data.getTask() != null ? Task2Data.fromJson(data.getTask()) : data.getTask());
    }
}
