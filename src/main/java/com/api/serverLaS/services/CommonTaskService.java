package com.api.serverLaS.services;

import com.api.serverLaS.data.NextTaskData;
import com.api.serverLaS.models.Task;
import com.api.serverLaS.repositories.SolutionRepository;
import com.api.serverLaS.repositories.TaskRepository;
import com.api.serverLaS.requests.GetNextTaskRequest;
import its.model.definition.Domain;
import its.reasoner.nodes.DecisionTreeReasoner;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;

@Service
public class CommonTaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private SolutionRepository solutionRepository;

    @Autowired
    public ErrorMessageService errorMessageService;

    public String generateErrorText(List<DecisionTreeReasoner.DecisionTreeEvaluationResult> branchResultNodes, Domain situationDomain, String uid, int taskId) {
        String errorText = "";
        int countErrors = 0;
        for(DecisionTreeReasoner.DecisionTreeEvaluationResult branchResultNode : branchResultNodes) {
            if(!branchResultNode.getNode().getValue() && branchResultNode.getNode().getMetadata().get("alias") != null && countErrors>=4) {
                countErrors++;
                continue;
            }
            if(!branchResultNode.getNode().getValue() && branchResultNode.getNode().getMetadata().get("alias") != null) {
                errorText += errorMessageService.generateMessage(branchResultNode.getNode().getMetadata().get("alias").toString(), branchResultNode.getVariablesSnapshot(), situationDomain) + "<br>";
                countErrors++;
            }
        }
        if(countErrors-4 == 1) {
            errorText += "И еще " + (countErrors-4) + " ошибка.";
        } else if (countErrors-4 >= 2 && countErrors-4 <= 4) {
            errorText += "И еще " + (countErrors-4) + " ошибки.";
        } else if (countErrors-4 >= 5) {
            errorText += "И еще " + (countErrors-4) + " ошибок.";
        }


        if(!solutionRepository.hasSolution(uid, taskId)) {
            solutionRepository.create(uid, taskId);
        }

        if(!errorText.isEmpty()) {
            solutionRepository.addCountOfMistakes(uid, taskId);
        }
        return errorText;
    }

    public NextTaskData getNext(GetNextTaskRequest getNextTaskRequest, int sectionId) {
        List<Task> tasks = taskRepository.getFreeList(sectionId, getNextTaskRequest.getUid());
        if(tasks.isEmpty()) {
            return new NextTaskData(-1, "", null);
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
        return new NextTaskData(task.getId(), taskInTtl, jsonobj);
    }
}
