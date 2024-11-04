package com.api.serverLaS.services;

import com.api.serverLaS.data.NextTaskData;
import com.api.serverLaS.data.Task3Data;
import com.api.serverLaS.requests.GetNextTaskRequest;
import com.api.serverLaS.requests.task3.AnswerDataRequest;
import com.api.serverLaS.requests.task3.CheckAnswerRequest;
import com.api.serverLaS.requests.task3.GetHintRequest;
import com.api.serverLaS.response.CheckAnswerResponse;
import com.api.serverLaS.response.GetNextTaskResponse;
import com.api.serverLaS.response.task3.GetHintResponse;
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
    public CommonTaskService commonTaskService;

    @Autowired
    public UtilService utilService;

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
            Collections.reverse(branchResultNodes);
            errorText += commonTaskService.generateErrorText(branchResultNodes, newSituationDomain, request.getUid(), request.getTaskId());
        }

        StringWriter stringWriter = new StringWriter();
        DomainRDFWriter.saveDomain(situationDomain, stringWriter, "poas:poas/", Set.of());
        return new CheckAnswerResponse(errorText.isEmpty(), errorText, stringWriter.toString());
    }

    public GetHintResponse getHint(GetHintRequest request) {
        String[] possibleAnswers = new String[]{"answerInput", "answerOutput", "answerMutable"};
        String correctAnswer = "";
        String hintText = "";
        Domain situationDomain = this.model.getDomain().copy();
        for(AnswerDataRequest userAnswer : request.getAnswers()) {
            for(String answer : possibleAnswers) {
                Domain newSituationDomain = situationDomain.copy();
                DomainRDFFiller.fillDomain(newSituationDomain,
                        ModelFactory.createDefaultModel().read(IOUtils.toInputStream(request.getTaskInTTL(), "UTF-8"), null, "TTL"),
                        Set.of(DomainRDFFiller.Option.NARY_RELATIONSHIPS_OLD_COMPAT),
                        null);
                if(userAnswer.getAnswer().equals(answer)) {
                    continue;
                }
                LearningSituation situation = new LearningSituation(newSituationDomain,
                        new HashMap<>(Map.of(
                                "answer", new ObjectRef(answer),
                                "var", new ObjectRef(userAnswer.getVar())
                        ))
                );
                List<DecisionTreeReasoner.DecisionTreeEvaluationResult> branchResultNodes = DecisionTreeReasoner.solve(model.getDecisionTree(), situation);
                Collections.reverse(branchResultNodes);
                hintText = this.generateHintText(branchResultNodes, newSituationDomain);
                if(!hintText.isEmpty()) {
                    correctAnswer = answer;
                    break;
                }
            }
            if(!correctAnswer.isEmpty()) {
                break;
            }
        }

        commonTaskService.addCountOfHintsToDB(correctAnswer, request.getUid(), request.getTaskId());

        StringWriter stringWriter = new StringWriter();
        DomainRDFWriter.saveDomain(situationDomain, stringWriter, "poas:poas/", Set.of(DomainRDFWriter.Option.NARY_RELATIONSHIPS_OLD_COMPAT));
        return new GetHintResponse(hintText, stringWriter.toString());
    }

    public GetNextTaskResponse getNext(GetNextTaskRequest getNextTaskRequest) {
        NextTaskData data = commonTaskService.getNext(getNextTaskRequest, 3);

        return new GetNextTaskResponse(data.getTaskId(), data.getTaskInTTL(), data.getTask() != null ? Task3Data.fromJson(data.getTask()) : data.getTask());
    }

    public String generateHintText(List<DecisionTreeReasoner.DecisionTreeEvaluationResult> branchResultNodes, Domain situationDomain) {
        String hintText = "";
        boolean isError = false;
        for(DecisionTreeReasoner.DecisionTreeEvaluationResult branchResultNode : branchResultNodes) {
            if (!branchResultNode.getNode().getValue() && branchResultNode.getNode().getMetadata().get("alias") != null) {
                isError = true;
                break;
            } else if (branchResultNode.getNode().getValue() && branchResultNode.getNode().getMetadata().get("alias") != null) {
                hintText += utilService.generateMessage(branchResultNode.getNode().getMetadata().get("alias").toString(), branchResultNode.getVariablesSnapshot(), situationDomain) + "<br>";
            }
        }
        if (isError) {
            return "";
        }
        return hintText;
    }
}
