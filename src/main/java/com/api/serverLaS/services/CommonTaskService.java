package com.api.serverLaS.services;

import its.model.definition.DomainModel;
import its.model.nodes.BranchResult;
import its.model.nodes.BranchResultNode;
import its.reasoner.nodes.DecisionTreeTrace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CommonTaskService {

    @Autowired
    public UtilService utilService;

    public String generateErrorText(List<DecisionTreeTrace> branchResultNodes, DomainModel situationDomain, String uid, int taskId, String answer) {
        String errorText = "";
        int countErrors = 0;
        for(DecisionTreeTrace branchResultNode : branchResultNodes) {
            if(!branchResultNode.getResultingElement().isAggregated() && branchResultNode.getBranchResult() == BranchResult.ERROR && branchResultNode.getResultingNode().getMetadata().get("alias") != null && countErrors>=4) {
                countErrors++;
                continue;
            }
            if(!branchResultNode.getResultingElement().isAggregated() && branchResultNode.getBranchResult() == BranchResult.ERROR && branchResultNode.getResultingNode().getMetadata().get("alias") != null) {
                String errorNodeText = utilService.generateMessage(branchResultNode.getResultingNode().getMetadata().get("alias").toString(), branchResultNode.getFinalVariableSnapshot(), situationDomain);
                if(!errorText.contains(errorNodeText)) {
                    errorText += errorNodeText + "<br><br>";
                    countErrors++;
                }
            }
        }
        if(countErrors-4 == 1) {
            errorText += "И еще " + (countErrors-4) + " ошибка.";
        } else if (countErrors-4 >= 2 && countErrors-4 <= 4) {
            errorText += "И еще " + (countErrors-4) + " ошибки.";
        } else if (countErrors-4 >= 5) {
            errorText += "И еще " + (countErrors-4) + " ошибок.";
        }

        return errorText;
    }

    public String generateHintText(List<DecisionTreeTrace> branchResultNodes, DomainModel situationDomain) {
        String hintText = "";
        boolean isError = false;
        for(DecisionTreeTrace branchResultNode : branchResultNodes) {
            if (branchResultNode.getBranchResult() == BranchResult.ERROR && branchResultNode.getResultingNode().getMetadata().get("alias") != null) {
                isError = true;
                break;
            } else if (branchResultNode.getBranchResult() == BranchResult.CORRECT && branchResultNode.getResultingNode().getMetadata().get("alias") != null && branchResultNode.getResultingNode() instanceof BranchResultNode) {
                hintText += utilService.generateMessage(branchResultNode.getResultingNode().getMetadata().get("alias").toString(), branchResultNode.getFinalVariableSnapshot(), situationDomain) + "<br><br>";
            }
        }
        if (isError) {
            return "";
        }
        return hintText;
    }

    public String[] getErrorLines(List<DecisionTreeTrace> branchResultNodes, DomainModel situationDomain, String nameVar) {
        ArrayList<String> lines = new ArrayList<String>();
        int countErrors = 0;
        for(DecisionTreeTrace branchResultNode : branchResultNodes) {
            if(!branchResultNode.getResultingElement().isAggregated() && branchResultNode.getBranchResult() == BranchResult.ERROR && branchResultNode.getResultingNode().getMetadata().get("alias") != null && countErrors>=4) {
                countErrors++;
                continue;
            }

            if(!branchResultNode.getResultingElement().isAggregated() && branchResultNode.getBranchResult() == BranchResult.ERROR && branchResultNode.getResultingNode().getMetadata().get("alias") != null) {
                lines.add(branchResultNode.getFinalVariableSnapshot().get(nameVar).findIn(situationDomain).getName());
                countErrors++;
            }
        }

        return lines.toArray(new String[0]);
    }

    public List<DecisionTreeTrace> getListDecisionTreeTrace(DecisionTreeTrace trace) {
        List<DecisionTreeTrace> branchResultNodes = new ArrayList<DecisionTreeTrace>();

        this.addListDecisionTreeTrace(branchResultNodes, trace);

        return branchResultNodes;
    }

    private void addListDecisionTreeTrace(List<DecisionTreeTrace> branchResultNodes, DecisionTreeTrace trace) {
        if(trace.getResultingElement().isAggregated()) {
            branchResultNodes.add(new DecisionTreeTrace(List.of(trace.get(trace.size() - 1))));
            for (DecisionTreeTrace decisionTreeTrace : trace.getResultingElement().nestedTraces()) {
                this.addListDecisionTreeTrace(branchResultNodes, decisionTreeTrace);
            }
        } else {
            branchResultNodes.add(trace);
        }
    }
}
