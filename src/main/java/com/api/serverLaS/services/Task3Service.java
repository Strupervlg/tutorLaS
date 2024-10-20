package com.api.serverLaS.services;

import com.api.serverLaS.data.NextTaskData;
import com.api.serverLaS.data.Task3Data;
import com.api.serverLaS.repositories.SolutionRepository;
import com.api.serverLaS.requests.GetNextTaskRequest;
import com.api.serverLaS.requests.task3.AnswerDataRequest;
import com.api.serverLaS.requests.task3.CheckAnswerRequest;
import com.api.serverLaS.response.CheckAnswerResponse;
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
public class Task3Service {

    @Autowired
    private SolutionRepository solutionRepository;

    @Autowired
    public CommonTaskService commonTaskService;

    public DomainSolvingModel model = new DomainSolvingModel(
				this.getClass().getClassLoader().getResource("Task3/"),
    DomainSolvingModel.BuildMethod.DICT_RDF
		);

    public CheckAnswerResponse checkAnswer(CheckAnswerRequest request) {
        String errorText = "";
        Domain situationDomain = this.model.getDomain().copy();
        for(AnswerDataRequest answer : request.getAnswers()) {
            Domain newSituationDomain = situationDomain.copy();
            DomainRDFFiller.fillDomain(newSituationDomain,
                    ModelFactory.createDefaultModel().read(IOUtils.toInputStream(request.getTaskInTTL(), "UTF-8"), null, "TTL"),
                    Set.of(DomainRDFFiller.Option.NARY_RELATIONSHIPS_OLD_COMPAT),
                    null);

            newSituationDomain.validateAndThrow();

            LearningSituation situation = new LearningSituation(newSituationDomain,
                    new HashMap<>(Map.of(
                            "answer", new ObjectRef(answer.getAnswer()),
                            "var", new ObjectRef(answer.getVar())
                    ))
            );

            List<DecisionTreeReasoner.DecisionTreeEvaluationResult> branchResultNodes = DecisionTreeReasoner.solve(model.getDecisionTree(), situation);
            errorText += commonTaskService.generateErrorText(branchResultNodes, newSituationDomain, request.getUid(), request.getTaskId());
        }

        StringWriter stringWriter = new StringWriter();
        DomainRDFWriter.saveDomain(situationDomain, stringWriter, "poas:poas/", Set.of());
        return new CheckAnswerResponse(errorText.isEmpty(), errorText, stringWriter.toString());
    }

//    public GetHintResponse getHint(GetHintRequest request) {
//        Domain situationDomain = this.model.getDomain().copy();
//        DomainRDFFiller.fillDomain(situationDomain,
//                ModelFactory.createDefaultModel().read(IOUtils.toInputStream(request.getTaskInTTL(), "UTF-8"), null, "TTL"),
//                Set.of(DomainRDFFiller.Option.NARY_RELATIONSHIPS_OLD_COMPAT),
//                null);
//
//        situationDomain.validateAndThrow();
//        String correctStep = "";
//        for(String step : request.getSteps()) {
//            LearningSituation situation = new LearningSituation(situationDomain,
//                    new HashMap<>(Map.of(
//                            "stepVar", new ObjectRef(request.getStepVar()),
//                            "step", new ObjectRef(step),
//                            "var", new ObjectRef(request.getVar())
//                    ))
//            );
//            boolean answer = DecisionTreeReasoner.getAnswer(model.getDecisionTree().getMainBranch(), situation);
//            if(answer) {
//                correctStep = step;
//                break;
//            }
//        }
//
//        if(!solutionRepository.hasSolution(request.getUid(), request.getTaskId())) {
//            solutionRepository.create(request.getUid(), request.getTaskId());
//        }
//
//        if(!correctStep.isEmpty()) {
//            solutionRepository.addCountOfHints(request.getUid(), request.getTaskId());
//        }
//
//        StringWriter stringWriter = new StringWriter();
//        DomainRDFWriter.saveDomain(situationDomain, stringWriter, "poas:poas/", Set.of());
//        return new GetHintResponse(correctStep, stringWriter.toString());
//    }

    public GetNextTaskResponse getNext(GetNextTaskRequest getNextTaskRequest) {
        NextTaskData data = commonTaskService.getNext(getNextTaskRequest, 3);

        return new GetNextTaskResponse(data.getTaskId(), data.getTaskInTTL(), data.getTask() != null ? Task3Data.fromJson(data.getTask()) : data.getTask());
    }
}
