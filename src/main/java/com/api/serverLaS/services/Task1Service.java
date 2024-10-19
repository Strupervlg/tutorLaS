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
public class Task1Service {

    @Autowired
    private SolutionRepository solutionRepository;

    @Autowired
    public CommonTaskService commonTaskService;

    public DomainSolvingModel model = new DomainSolvingModel(
				this.getClass().getClassLoader().getResource("Task1/"),
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
                        "stepVar", new ObjectRef(request.getStepVar()),
                        "step", new ObjectRef(request.getStep()),
                        "var", new ObjectRef(request.getVar())
                ))
        );

        List<DecisionTreeReasoner.DecisionTreeEvaluationResult> branchResultNodes = DecisionTreeReasoner.solve(model.getDecisionTree(), situation);
        String errorText = commonTaskService.generateErrorText(branchResultNodes, situationDomain, request.getUid(), request.getTaskId());

        StringWriter stringWriter = new StringWriter();
        DomainRDFWriter.saveDomain(situationDomain, stringWriter, "poas:poas/", Set.of());
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
                        "stepVar", new ObjectRef(request.getStepVar()),
                        "var", new ObjectRef(request.getVar())
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
        String correctStep = "";
        for(String step : request.getSteps()) {
            LearningSituation situation = new LearningSituation(situationDomain,
                    new HashMap<>(Map.of(
                            "stepVar", new ObjectRef(request.getStepVar()),
                            "step", new ObjectRef(step),
                            "var", new ObjectRef(request.getVar())
                    ))
            );
            boolean answer = DecisionTreeReasoner.getAnswer(model.getDecisionTree().getMainBranch(), situation);
            if(answer) {
                correctStep = step;
                break;
            }
        }

        if(!solutionRepository.hasSolution(request.getUid(), request.getTaskId())) {
            solutionRepository.create(request.getUid(), request.getTaskId());
        }

        if(!correctStep.isEmpty()) {
            solutionRepository.addCountOfHints(request.getUid(), request.getTaskId());
        }

        StringWriter stringWriter = new StringWriter();
        DomainRDFWriter.saveDomain(situationDomain, stringWriter, "poas:poas/", Set.of());
        return new GetHintResponse(correctStep, stringWriter.toString());
    }

    public GetNextTaskResponse getNext(GetNextTaskRequest getNextTaskRequest) {
        NextTaskData data = commonTaskService.getNext(getNextTaskRequest, 1);

        return new GetNextTaskResponse(data.getTaskId(), data.getTaskInTTL(), data.getTask() != null ? Task1Data.fromJson(data.getTask()) : data.getTask());
    }
}
