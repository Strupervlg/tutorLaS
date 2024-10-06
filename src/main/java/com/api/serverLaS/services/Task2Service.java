package com.api.serverLaS.services;

import com.api.serverLaS.data.Task2Data;
import com.api.serverLaS.models.Task;
import com.api.serverLaS.repositories.SolutionRepository;
import com.api.serverLaS.repositories.TaskRepository;
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

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import jakarta.json.*;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Service
public class Task2Service {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private SolutionRepository solutionRepository;

    @Autowired
    public ErrorMessageService errorMessageService;

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
        String errorText = "";
        for(DecisionTreeReasoner.DecisionTreeEvaluationResult branchResultNode : branchResultNodes) {
            if(!branchResultNode.getNode().getValue() && branchResultNode.getNode().getMetadata().get("alias") != null) {
                errorText += errorMessageService.generateMessage(branchResultNode.getNode().getMetadata().get("alias").toString(), branchResultNode.getVariablesSnapshot(), situationDomain)  + "<br>";
            }
        }

        if(!solutionRepository.hasSolution(request.getUid(), request.getTaskId())) {
            solutionRepository.create(request.getUid(), request.getTaskId());
        }

        if(errorText.isEmpty()) {
            solutionRepository.addCountOfCorrect(request.getUid(), request.getTaskId());
        } else {
            solutionRepository.addCountOfMistakes(request.getUid(), request.getTaskId());
        }

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
        String errorText = "";
        branchResultNodes.sort(
                Comparator.comparingInt(
                        (DecisionTreeReasoner.DecisionTreeEvaluationResult node) -> {
                            if(node.getVariablesSnapshot().get("step") != null) {
                                return (int) node.getVariablesSnapshot().get("step").findIn(situationDomain).getPropertyValue("number");
                            } else {
                                return 0;
                            }}));
        for(DecisionTreeReasoner.DecisionTreeEvaluationResult branchResultNode : branchResultNodes) {
            if(!branchResultNode.getNode().getValue() && branchResultNode.getNode().getMetadata().get("alias") != null) {
                errorText += errorMessageService.generateMessage(branchResultNode.getNode().getMetadata().get("alias").toString(), branchResultNode.getVariablesSnapshot(), situationDomain) + "<br>";
            }
        }

        if(!solutionRepository.hasSolution(request.getUid(), request.getTaskId())) {
            solutionRepository.create(request.getUid(), request.getTaskId());
        }

        if(!errorText.isEmpty()) {
            solutionRepository.addCountOfMistakes(request.getUid(), request.getTaskId());
        }

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
        for(String line : request.getLines()) {
            LearningSituation situation = new LearningSituation(situationDomain,
                    new HashMap<>(Map.of(
                            "usageLine", new ObjectRef(line),
                            "var", new ObjectRef(request.getVar()),
                            "prefix", new ObjectRef(request.getPrefix())
                    ))
            );
            boolean answer = DecisionTreeReasoner.getAnswer(model.getDecisionTree().getMainBranch(), situation);

            if(answer) {
                correctLine = line;
                break;
            }
        }

        if(!solutionRepository.hasSolution(request.getUid(), request.getTaskId())) {
            solutionRepository.create(request.getUid(), request.getTaskId());
        }

        if(!correctLine.isEmpty()) {
            solutionRepository.addCountOfHints(request.getUid(), request.getTaskId());
        }

        StringWriter stringWriter = new StringWriter();
        DomainRDFWriter.saveDomain(situationDomain, stringWriter, "poas:poas/", Set.of(DomainRDFWriter.Option.NARY_RELATIONSHIPS_OLD_COMPAT));
        return new GetHintResponse(correctLine, stringWriter.toString());
    }

    //TODO поменять функцию можно общую часть вынести
    public GetNextTaskResponse getNext(GetNextTaskRequest getNextTaskRequest) {
        List<Task> tasks = taskRepository.getFreeList(2, getNextTaskRequest.getUid());

        if(tasks.isEmpty()) {
            return new GetNextTaskResponse(-1, "", null);
        }
        Random random = new Random();
        int randomIndex = random.nextInt(tasks.size());
        Task task = tasks.get(randomIndex);

        JsonReader reader;
        try {
            reader = Json.createReader(new FileReader(this.getClass().getClassLoader().getResource("tasks/"+task.getName()+"/").getPath() + task.getNameJson()));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        String taskInTtl = "";
        try {
            String fileTtl = this.getClass().getClassLoader().getResource("tasks/"+task.getName()+"/").getPath() + task.getNameTtl();
            taskInTtl = new String(Files.readAllBytes(Paths.get(fileTtl)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        JsonObject jsonobj = reader.read().asJsonObject();

        return new GetNextTaskResponse(task.getId(), taskInTtl, Task2Data.fromJson(jsonobj));
    }
}
