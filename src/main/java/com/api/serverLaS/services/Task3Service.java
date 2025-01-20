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
import its.model.definition.DomainModel;
import its.model.definition.ObjectRef;
import its.model.definition.rdf.DomainRDFFiller;
import its.model.definition.rdf.DomainRDFWriter;
import its.model.nodes.BranchResult;
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
public class Task3Service {

    @Autowired
    public CommonTaskService commonTaskService;

    @Autowired
    public UtilService utilService;

    public DomainSolvingModel model = new DomainSolvingModel(
            "./Task3",
            DomainSolvingModel.BuildMethod.DICT_RDF
		);

    public CheckAnswerResponse checkAnswer(CheckAnswerRequest request) {
        String errorText = "";
        DomainModel situationDomain = this.model.getDomainModel().copy();
        for(AnswerDataRequest answer : request.getAnswers()) {
            DomainModel newSituationDomain = situationDomain.copy();
            try {
                DomainRDFFiller.fillDomain(newSituationDomain,
                        ModelFactory.createDefaultModel().read(IOUtils.toInputStream(request.getTaskInTTL(), "UTF-8"), null, "TTL"),
                        Set.of(DomainRDFFiller.Option.NARY_RELATIONSHIPS_OLD_COMPAT),
                        null);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            newSituationDomain.validateAndThrow();

            LearningSituation situation = new LearningSituation(newSituationDomain,
                    new HashMap<>(Map.of(
                            "answer", new ObjectRef(answer.getAnswer()),
                            "var", new ObjectRef(answer.getVar())
                    ))
            );
            BranchResultProcessor resultProcessor = new BranchResultProcessor();
            DecisionTreeReasoner.solve(model.getDecisionTree(), situation, resultProcessor);
            List<DecisionTreeEvaluationResult<BranchResultNode>> branchResultNodes= resultProcessor.getList();
            Collections.reverse(branchResultNodes);
            errorText += commonTaskService.generateErrorText(resultProcessor.getList(), newSituationDomain, request.getUid(), request.getTaskId(), answer.getVar()+"_"+answer.getAnswer());
            String correctText = commonTaskService.generateHintText(resultProcessor.getList(), newSituationDomain);
            commonTaskService.addCountOfCorrectToDB(errorText, request.getUid(), request.getTaskId(), answer.getVar()+"_"+answer.getAnswer(), correctText);
        }

        StringWriter stringWriter = new StringWriter();
        DomainRDFWriter.saveDomain(situationDomain, stringWriter, "poas:poas/", Set.of());
        return new CheckAnswerResponse(errorText.isEmpty(), errorText, stringWriter.toString());
    }

    public GetHintResponse getHint(GetHintRequest request) {
        String[] possibleAnswers = new String[]{"answerInput", "answerOutput", "answerMutable"};
        String correctAnswer = "";
        String hintText = "";
        DomainModel situationDomain = this.model.getDomainModel().copy();
        for(AnswerDataRequest userAnswer : request.getAnswers()) {
            for(String answer : possibleAnswers) {
                DomainModel newSituationDomain = situationDomain.copy();
                try {
                    DomainRDFFiller.fillDomain(newSituationDomain,
                            ModelFactory.createDefaultModel().read(IOUtils.toInputStream(request.getTaskInTTL(), "UTF-8"), null, "TTL"),
                            Set.of(DomainRDFFiller.Option.NARY_RELATIONSHIPS_OLD_COMPAT),
                            null);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                if(userAnswer.getAnswer().equals(answer)) {
                    continue;
                }
                LearningSituation situation = new LearningSituation(newSituationDomain,
                        new HashMap<>(Map.of(
                                "answer", new ObjectRef(answer),
                                "var", new ObjectRef(userAnswer.getVar())
                        ))
                );
                BranchResultProcessor resultProcessor = new BranchResultProcessor();
                DecisionTreeReasoner.solve(model.getDecisionTree(), situation, resultProcessor);
                List<DecisionTreeEvaluationResult<BranchResultNode>> branchResultNodes= resultProcessor.getList();
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

        commonTaskService.addCountOfHintsToDB(correctAnswer, request.getUid(), request.getTaskId(), hintText);

        StringWriter stringWriter = new StringWriter();
        DomainRDFWriter.saveDomain(situationDomain, stringWriter, "poas:poas/", Set.of(DomainRDFWriter.Option.NARY_RELATIONSHIPS_OLD_COMPAT));
        return new GetHintResponse(hintText, stringWriter.toString());
    }

    public GetNextTaskResponse getNext(GetNextTaskRequest getNextTaskRequest) {
        NextTaskData data = commonTaskService.getNext(getNextTaskRequest, 3);
        if(data.getTaskId() == -1) {
            data = commonTaskService.getNext(getNextTaskRequest, 31);
        }

        return new GetNextTaskResponse(data.getTaskId(), data.getTaskInTTL(), data.getTask() != null ? Task3Data.fromJson(data.getTask()) : data.getTask());
    }

    public String generateHintText(List<DecisionTreeEvaluationResult<BranchResultNode>> branchResultNodes, DomainModel situationDomain) {
        String hintText = "";
        boolean isError = false;
        for(DecisionTreeEvaluationResult<BranchResultNode> branchResultNode : branchResultNodes) {
            if (branchResultNode.getValue() == BranchResult.ERROR && branchResultNode.getNode().getMetadata().get("alias") != null) {
                isError = true;
                break;
            } else if (branchResultNode.getNode().getValue() == BranchResult.CORRECT && branchResultNode.getNode().getMetadata().get("alias") != null) {
                hintText += utilService.generateMessage(branchResultNode.getNode().getMetadata().get("alias").toString(), branchResultNode.getVariablesSnapshot(), situationDomain) + "<br><br>";
            }
        }
        if (isError) {
            return "";
        }
        return hintText;
    }
}
