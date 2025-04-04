package com.api.serverLaS;

import com.api.serverLaS.services.CommonTaskService;
import com.api.serverLaS.services.UtilService;
import its.model.DomainSolvingModel;
import its.model.definition.DomainModel;
import its.model.definition.ObjectRef;
import its.model.definition.rdf.DomainRDFFiller;
import its.model.nodes.BranchResult;
import its.reasoner.LearningSituation;
import its.reasoner.nodes.DecisionTreeReasoner;
import its.reasoner.nodes.DecisionTreeTrace;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;
import java.util.stream.Stream;

@SpringBootTest
class Task3Tests {

	@Autowired
	private CommonTaskService commonTaskService;

	@Autowired
	private UtilService utilService;

	public DomainSolvingModel model = new DomainSolvingModel(
			this.getClass().getClassLoader().getResource("Task3/"),
			DomainSolvingModel.BuildMethod.DICT_RDF
	);

	static Stream<Arguments> provideTestAnswerTaskCases() {
		return Stream.of(
				Arguments.of("31.ttl", "varD3", "answerInput", BranchResult.CORRECT),
				Arguments.of("31.ttl", "varV1", "answerOutput", BranchResult.CORRECT),
				Arguments.of("31.ttl", "varV1", "answerInput", BranchResult.ERROR),
				Arguments.of("31.ttl", "varV1", "answerMutable", BranchResult.ERROR),
				Arguments.of("32.ttl", "varD3", "answerMutable", BranchResult.CORRECT)
		);
	}

	static Stream<Arguments> provideTestHintTaskCases() {
		return Stream.of(
				Arguments.of("31.ttl", "varD3", "answerInput", "Переменная \"d\" является входной, так как во всех местах её использования она используется только как входная.<br>"),
				Arguments.of("31.ttl", "varV1", "answerOutput", "Переменная \"v\" является выходной, так как во всех местах её использования она используется только как выходная.<br>"),
				Arguments.of("32.ttl", "varD3", "answerMutable", "Переменная \"d\" является изменяемой, так как:<br>Переменная \"d\" на позиции 3 является левым операндом оператора \"-\" и считается входной.<br>Переменная \"d\" на позиции 8 является 3 аргументом функции \"test\" и является выходной.<br>Переменная \"d\" на позиции 10 является правым операндом оператора \"*\" и считается входной.<br>")
		);
	}

	static Stream<Arguments> provideTestErrorTaskCases() {
		return Stream.of(
				Arguments.of("31.ttl", "varV1", "answerInput", "Переменная \"v\" не является входной, так как:<br>Переменная \"v\" на позиции 1 является левым операндом оператора \"=\" и является выходной.<br>"),
				Arguments.of("31.ttl", "varV1", "answerMutable", "Переменная \"v\" не является изменяемой, так как во всех местах её использования она используется только как выходная.<br>"),
				Arguments.of("32.ttl", "varD3", "answerInput", "Переменная \"d\" не является входной, так как:<br>Переменная \"d\" на позиции 8 является 3 аргументом функции \"test\" и является выходной.<br>"),
				Arguments.of("32.ttl", "varD3", "answerOutput", "Переменная \"d\" не является выходной, так как:<br>Переменная \"d\" на позиции 10 является правым операндом оператора \"*\" и является входной.<br>Переменная \"d\" на позиции 3 является левым операндом оператора \"-\" и является входной.<br>")
		);
	}

	@ParameterizedTest
	@MethodSource("provideTestAnswerTaskCases")
	void testAnswerTask(String nameTtl, String var, String answer, BranchResult expResult) {
		DomainModel situationDomain = this.model.getDomainModel().copy();
		DomainRDFFiller.fillDomain(situationDomain, this.getClass().getClassLoader().getResource("tasks/testTasks/").getPath() + nameTtl, Set.of(DomainRDFFiller.Option.NARY_RELATIONSHIPS_OLD_COMPAT), null);

		situationDomain.validateAndThrow();

		LearningSituation situation = new LearningSituation(situationDomain,
				new HashMap<>(Map.of(
						"answer", new ObjectRef(answer),
						"var", new ObjectRef(var)
				))
		);

		DecisionTreeTrace result = DecisionTreeReasoner.solve(model.getDecisionTree(), situation);
        Assertions.assertEquals(expResult, result.getBranchResult());
	}

	@ParameterizedTest
	@MethodSource("provideTestHintTaskCases")
	void testHintTask(String nameTtl, String var, String answer, String expHint) {
		DomainModel situationDomain = this.model.getDomainModel().copy();
		DomainRDFFiller.fillDomain(situationDomain, this.getClass().getClassLoader().getResource("tasks/testTasks/").getPath() + nameTtl, Set.of(DomainRDFFiller.Option.NARY_RELATIONSHIPS_OLD_COMPAT), null);

		situationDomain.validateAndThrow();

		LearningSituation situation = new LearningSituation(situationDomain,
				new HashMap<>(Map.of(
						"answer", new ObjectRef(answer),
						"var", new ObjectRef(var)
				))
		);
		DecisionTreeTrace result = DecisionTreeReasoner.solve(model.getDecisionTree(), situation);
		List<DecisionTreeTrace> branchResultNodes = new ArrayList<DecisionTreeTrace>();
		if(!result.getResultingElement().isAggregated()) {
			for (DecisionTreeTrace decisionTreeTrace : result.get(1).nestedTraces()) {
				branchResultNodes.add(decisionTreeTrace);
			}
			branchResultNodes.add(result);
		} else {
			branchResultNodes = commonTaskService.getListDecisionTreeTrace(result);
		}
		Collections.reverse(branchResultNodes);
		String hintText = "";
		for(DecisionTreeTrace branchResultNode : branchResultNodes) {
			if (branchResultNode.getBranchResult() == BranchResult.ERROR && branchResultNode.getResultingNode().getMetadata().get("alias") != null) {
				break;
			} else if (branchResultNode.getBranchResult() == BranchResult.CORRECT && branchResultNode.getResultingNode().getMetadata().get("alias") != null) {
				hintText += utilService.generateMessage(branchResultNode.getResultingNode().getMetadata().get("alias").toString(), branchResultNode.getFinalVariableSnapshot(), situationDomain) + "<br>";
			}
		}
		Assertions.assertEquals(expHint, hintText);
	}

	@ParameterizedTest
	@MethodSource("provideTestErrorTaskCases")
	void testErrorTask(String nameTtl, String var, String answer, String expError) {
		DomainModel situationDomain = this.model.getDomainModel().copy();
		DomainRDFFiller.fillDomain(situationDomain, this.getClass().getClassLoader().getResource("tasks/testTasks/").getPath() + nameTtl, Set.of(DomainRDFFiller.Option.NARY_RELATIONSHIPS_OLD_COMPAT), null);

		situationDomain.validateAndThrow();

		LearningSituation situation = new LearningSituation(situationDomain,
				new HashMap<>(Map.of(
						"answer", new ObjectRef(answer),
						"var", new ObjectRef(var)
				))
		);

		DecisionTreeTrace result = DecisionTreeReasoner.solve(model.getDecisionTree(), situation);
		List<DecisionTreeTrace> branchResultNodes = commonTaskService.getListDecisionTreeTrace(result);
		String errorText = "";
		for(DecisionTreeTrace branchResultNode : branchResultNodes) {
			if(!branchResultNode.getResultingElement().isAggregated() && branchResultNode.getBranchResult() == BranchResult.ERROR && branchResultNode.getResultingNode().getMetadata().get("alias") != null) {
				errorText += utilService.generateMessage(branchResultNode.getResultingNode().getMetadata().get("alias").toString(), branchResultNode.getFinalVariableSnapshot(), situationDomain) + "<br>";
			}
		}
		Assertions.assertEquals(expError, errorText);
	}
}
