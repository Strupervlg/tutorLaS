package com.api.serverLaS.services;

import com.api.serverLaS.data.Task1Data;
import com.api.serverLaS.data.Task3Data;
import com.api.serverLaS.models.Task;
import com.api.serverLaS.repositories.SolutionRepository;
import com.api.serverLaS.repositories.TaskRepository;
import com.api.serverLaS.requests.GetNextTaskRequest;
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
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.ModelFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Service
public class Task3Service {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private SolutionRepository solutionRepository;

    @Autowired
    public ErrorMessageService errorMessageService;

    public DomainSolvingModel model = new DomainSolvingModel(
				this.getClass().getClassLoader().getResource("Task3/"),
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
                        "answer", new ObjectRef(request.getAnswer()),
                        "var", new ObjectRef(request.getVar())
                ))
        );

        List<DecisionTreeReasoner.DecisionTreeEvaluationResult> branchResultNodes = DecisionTreeReasoner.solve(model.getDecisionTree(), situation);
        String errorText = "";
        for(DecisionTreeReasoner.DecisionTreeEvaluationResult branchResultNode : branchResultNodes) {
            if(!branchResultNode.getNode().getValue() && branchResultNode.getNode().getMetadata().get("alias") != null) {
                errorText += errorMessageService.generateMessage(branchResultNode.getNode().getMetadata().get("alias").toString(), branchResultNode.getVariablesSnapshot(), situationDomain)  + "<br>";
//                errorText += branchResultNode.getNode().getMetadata().get("alias").toString()  + "<br>";
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
        List<Task> tasks = taskRepository.getFreeList(3, getNextTaskRequest.getUid());
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

        return new GetNextTaskResponse(task.getId(), taskInTtl, Task3Data.fromJson(jsonobj));
    }
}
