package com.api.serverLaS.services;

import com.api.serverLaS.data.NextTaskData;
import com.api.serverLaS.models.Task;
import com.api.serverLaS.repositories.SolutionRepository;
import com.api.serverLaS.repositories.TaskRepository;
import com.api.serverLaS.requests.GetNextTaskRequest;
import its.model.definition.DomainModel;
import its.model.nodes.BranchResult;
import its.model.nodes.BranchResultNode;
import its.reasoner.nodes.DecisionTreeEvaluationResult;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class CommonTaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private SolutionRepository solutionRepository;

    @Autowired
    public UtilService utilService;

    public String generateErrorText(List<DecisionTreeEvaluationResult<BranchResultNode>> branchResultNodes, DomainModel situationDomain, String uid, int taskId, String answer) {
        String errorText = "";
        int countErrors = 0;
        for(DecisionTreeEvaluationResult<BranchResultNode> branchResultNode : branchResultNodes) {
            if(branchResultNode.getValue() == BranchResult.ERROR && branchResultNode.getNode().getMetadata().get("alias") != null && countErrors>=4) {
                countErrors++;
                continue;
            }
            if(branchResultNode.getValue() == BranchResult.ERROR && branchResultNode.getNode().getMetadata().get("alias") != null) {
                errorText += utilService.generateMessage(branchResultNode.getNode().getMetadata().get("alias").toString(), branchResultNode.getVariablesSnapshot(), situationDomain) + "<br>";
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

        this.addCountOfMistakesToDB(errorText, uid, taskId, answer);

        return errorText;
    }

    public NextTaskData getNext(GetNextTaskRequest getNextTaskRequest, int sectionId) {
        if(taskRepository.hasCorrectTask(sectionId, getNextTaskRequest.getUid())) {
            return new NextTaskData(-1, "", null);
        }
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

    public String generateHintText(List<DecisionTreeEvaluationResult<BranchResultNode>> branchResultNodes, DomainModel situationDomain) {
        String hintText = "";
        for(DecisionTreeEvaluationResult<BranchResultNode> branchResultNode : branchResultNodes) {
            if (branchResultNode.getValue() == BranchResult.ERROR && branchResultNode.getNode().getMetadata().get("alias") != null) {
                break;
            } else if (branchResultNode.getValue() == BranchResult.CORRECT && branchResultNode.getNode().getMetadata().get("alias") != null) {
                hintText = utilService.generateMessage(branchResultNode.getNode().getMetadata().get("alias").toString(), branchResultNode.getVariablesSnapshot(), situationDomain);
                break;
            }
        }
        return hintText;
    }

    public void addCountOfCorrectToDB(String errorText, String uid, int taskId, String answer, String correctText) {
        if(!solutionRepository.hasSolution(uid, taskId)) {
            solutionRepository.create(uid, taskId);
        }

        if(errorText.isEmpty()) {
            solutionRepository.addCountOfCorrect(uid, taskId, answer, correctText);
        }
    }

    public void addCountOfMistakesToDB(String errorText, String uid, int taskId, String answer) {
        if(!solutionRepository.hasSolution(uid, taskId)) {
            solutionRepository.create(uid, taskId);
        }

        if(!errorText.isEmpty()) {
            solutionRepository.addCountOfMistakes(uid, taskId, answer, errorText);
        }
    }

    public void addCountOfHintsToDB(String correctAnswer, String uid, int taskId, String hintText) {
        if(!solutionRepository.hasSolution(uid, taskId)) {
            solutionRepository.create(uid, taskId);
        }

        if(!correctAnswer.isEmpty()) {
            solutionRepository.addCountOfHints(uid, taskId, correctAnswer, hintText);
        }
    }

    public String[] getErrorLines(List<DecisionTreeEvaluationResult<BranchResultNode>> branchResultNodes, DomainModel situationDomain, String nameVar) {
        ArrayList<String> lines = new ArrayList<String>();
        int countErrors = 0;
        for(DecisionTreeEvaluationResult<BranchResultNode> branchResultNode : branchResultNodes) {
            if(branchResultNode.getValue() == BranchResult.ERROR && branchResultNode.getNode().getMetadata().get("alias") != null && countErrors>=4) {
                countErrors++;
                continue;
            }
            if(branchResultNode.getValue() == BranchResult.ERROR && branchResultNode.getNode().getMetadata().get("alias") != null) {
                lines.add(branchResultNode.getVariablesSnapshot().get(nameVar).findIn(situationDomain).getName());
                countErrors++;
            }
        }

        return lines.toArray(new String[0]);
    }
}
