package com.api.serverLaS.services;

import com.api.serverLaS.data.NextTaskData;
import com.api.serverLaS.data.Task1Data;
import com.api.serverLaS.repositories.SolutionRepository;
import com.api.serverLaS.requests.task1.CheckAnswerRequest;
import com.api.serverLaS.requests.task1.CompleteTaskRequest;
import com.api.serverLaS.requests.task1.GetHintRequest;
import com.api.serverLaS.requests.GetNextTaskRequest;
import com.api.serverLaS.response.CheckAnswerResponse;
import com.api.serverLaS.response.CompleteTaskResponse;
import com.api.serverLaS.response.task1.GetHintResponse;
import com.api.serverLaS.response.GetNextTaskResponse;
import its.model.DomainSolvingModel;
import its.model.definition.DomainModel;
import its.model.definition.ObjectRef;
import its.model.definition.rdf.DomainRDFFiller;
import its.model.definition.rdf.DomainRDFWriter;
import its.reasoner.LearningSituation;
import its.reasoner.nodes.DecisionTreeReasoner;
import its.reasoner.nodes.DecisionTreeTrace;
import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.ModelFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

@Service
public class Task1Service {

    @Autowired
    private SolutionRepository solutionRepository;

    @Autowired
    public CommonTaskService commonTaskService;

    public DomainSolvingModel model = new DomainSolvingModel(
            "./Task1",
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
                        "stepVar", new ObjectRef(request.getStepVar()),
                        "step", new ObjectRef(request.getStep()),
                        "var", new ObjectRef(request.getVar())
                ))
        );

        DecisionTreeTrace result = DecisionTreeReasoner.solve(model.getDecisionTree(), situation);
        List<DecisionTreeTrace> branchResultNodes = commonTaskService.getListDecisionTreeTrace(result);

        String errorText = commonTaskService.generateErrorText(branchResultNodes, situationDomain, request.getUid(), request.getTaskId(), request.getStep());
        String correctText = commonTaskService.generateHintText(branchResultNodes, situationDomain);
        commonTaskService.addCountOfCorrectToDB(errorText, request.getUid(), request.getTaskId(), request.getStep(), correctText);

        StringWriter stringWriter = new StringWriter();
        DomainRDFWriter.saveDomain(situationDomain, stringWriter, "poas:poas/", Set.of());
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
                        "stepVar", new ObjectRef(request.getStepVar()),
                        "var", new ObjectRef(request.getVar())
                ))
        );
        DecisionTreeTrace result = DecisionTreeReasoner.solve(model.getDecisionTrees().get("all"), situation);
        List<DecisionTreeTrace> branchResultNodes = commonTaskService.getListDecisionTreeTrace(result);

        branchResultNodes.sort(
                Comparator.comparingInt(
                        (DecisionTreeTrace node) -> {
                            if(node.getFinalVariableSnapshot().get("step") != null) {
                                return (int) node.getFinalVariableSnapshot().get("step").findIn(situationDomain).getPropertyValue("number", Map.of());
                            } else {
                                return 0;
                            }}));
        String errorText = commonTaskService.generateErrorText(branchResultNodes, situationDomain, request.getUid(), request.getTaskId(), "all");
        String[] steps = commonTaskService.getErrorLines(branchResultNodes, situationDomain, "step");

        return new CompleteTaskResponse(errorText.isEmpty(), errorText, steps);
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
        String correctStep = "";
        String hintText = "";
        for(String step : request.getSteps()) {
            LearningSituation situation = new LearningSituation(situationDomain,
                    new HashMap<>(Map.of(
                            "stepVar", new ObjectRef(request.getStepVar()),
                            "step", new ObjectRef(step),
                            "var", new ObjectRef(request.getVar())
                    ))
            );
            DecisionTreeTrace result = DecisionTreeReasoner.solve(model.getDecisionTree(), situation);
            List<DecisionTreeTrace> branchResultNodes = commonTaskService.getListDecisionTreeTrace(result);

            hintText = commonTaskService.generateHintText(branchResultNodes, situationDomain);
            if(!hintText.isEmpty()) {
                correctStep = step;
                break;
            }
        }

        if(hintText.isEmpty()) {
            LearningSituation situation = new LearningSituation(situationDomain,
                    new HashMap<>(Map.of(
                            "stepVar", new ObjectRef(request.getStepVar()),
                            "var", new ObjectRef(request.getVar())
                    ))
            );
            DecisionTreeTrace result = DecisionTreeReasoner.solve(model.getDecisionTrees().get("all"), situation);
            List<DecisionTreeTrace> branchResultNodes = commonTaskService.getListDecisionTreeTrace(result);

            hintText = commonTaskService.generateHintText(branchResultNodes, situationDomain);
        }

        commonTaskService.addCountOfHintsToDB(correctStep, request.getUid(), request.getTaskId(), hintText);

        StringWriter stringWriter = new StringWriter();
        DomainRDFWriter.saveDomain(situationDomain, stringWriter, "poas:poas/", Set.of());
        return new GetHintResponse(correctStep, hintText, stringWriter.toString());
    }

    public GetNextTaskResponse getNext(GetNextTaskRequest getNextTaskRequest) {
        Random random = new Random();
        List<Integer> sectionsIds = List.of(1, 11);
        int sectionId = sectionsIds.get(random.nextInt(sectionsIds.size()));

        NextTaskData data = commonTaskService.getNext(getNextTaskRequest, sectionId);

        return new GetNextTaskResponse(data.getTaskId(), data.getTaskInTTL(), data.getTask() != null ? Task1Data.fromJson(data.getTask()) : data.getTask());
    }

    public GetNextTaskResponse getEnNext(GetNextTaskRequest getNextTaskRequest) {
        NextTaskData data = commonTaskService.getNext(getNextTaskRequest, 111);

        return new GetNextTaskResponse(data.getTaskId(), data.getTaskInTTL(), data.getTask() != null ? Task1Data.fromJson(data.getTask()) : data.getTask());
    }
}
