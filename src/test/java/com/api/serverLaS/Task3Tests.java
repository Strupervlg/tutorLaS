package com.api.serverLaS;

import com.api.serverLaS.services.CommonTaskService;
import com.api.serverLaS.services.UtilService;
import its.model.DomainSolvingModel;
import its.model.definition.Domain;
import its.model.definition.ObjectRef;
import its.model.definition.rdf.DomainRDFFiller;
import its.reasoner.LearningSituation;
import its.reasoner.nodes.DecisionTreeReasoner;
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
				Arguments.of("31.ttl", "varD3", "answerInput", true),
				Arguments.of("31.ttl", "varV1", "answerOutput", true),
				Arguments.of("31.ttl", "varV1", "answerInput", false),
				Arguments.of("31.ttl", "varV1", "answerMutable", false),
				Arguments.of("32.ttl", "varD3", "answerMutable", true)
		);
	}

	static Stream<Arguments> provideTestHintTaskCases() {
		return Stream.of(
				Arguments.of("31.ttl", "varD3", "answerInput", "Переменная \"d\" является входной.<br>"),
				Arguments.of("31.ttl", "varV1", "answerOutput", "Переменная \"v\" является выходной.<br>"),
				Arguments.of("32.ttl", "varD3", "answerMutable", "Переменная \"d\" является изменяемой, так как:<br>Переменная \"d\" на позиции 3 является левым операндом оператора \"-\" и считается входной.<br>Переменная \"d\" на позиции 8 является 3 аргументом функции \"test\" и является выходной.<br>Переменная \"d\" на позиции 10 является правым операндом оператора \"*\" и считается входной.<br>")
		);
	}

	static Stream<Arguments> provideTestErrorTaskCases() {
		return Stream.of(
				Arguments.of("31.ttl", "varV1", "answerInput", "Переменная \"v\" на позиции 1 является левым операндом оператора \"=\" и считается выходной.<br>"),
				Arguments.of("31.ttl", "varV1", "answerMutable", "Переменная \"v\" не является изменяемой, так как во всех местах её использования она является выходной.<br>"),
				Arguments.of("32.ttl", "varD3", "answerInput", "Переменная \"d\" является изменяемой, так как:<br>Переменная \"d\" на позиции 8 является 3 аргументом функции \"test\" и является выходной.<br>"),
				Arguments.of("32.ttl", "varD3", "answerOutput", "Переменная \"d\" является изменяемой, так как:<br>Переменная \"d\" на позиции 3 является левым операндом оператора \"-\" и считается входной.<br>Переменная \"d\" на позиции 10 является правым операндом оператора \"*\" и считается входной.<br>")
		);
	}

	@ParameterizedTest
	@MethodSource("provideTestAnswerTaskCases")
	void testAnswerTask(String nameTtl, String var, String answer, boolean expResult) {
		Domain situationDomain = this.model.getDomain().copy();
		DomainRDFFiller.fillDomain(situationDomain, this.getClass().getClassLoader().getResource("tasks/testTasks/").getPath() + nameTtl, Set.of(DomainRDFFiller.Option.NARY_RELATIONSHIPS_OLD_COMPAT), null);

		situationDomain.validateAndThrow();

		LearningSituation situation = new LearningSituation(situationDomain,
				new HashMap<>(Map.of(
						"answer", new ObjectRef(answer),
						"var", new ObjectRef(var)
				))
		);

		boolean branchResultNodes = DecisionTreeReasoner.getAnswer(model.getDecisionTree().getMainBranch(), situation);
		Assertions.assertEquals(expResult, branchResultNodes);
	}

	@ParameterizedTest
	@MethodSource("provideTestHintTaskCases")
	void testHintTask(String nameTtl, String var, String answer, String expHint) {
		Domain situationDomain = this.model.getDomain().copy();
		DomainRDFFiller.fillDomain(situationDomain, this.getClass().getClassLoader().getResource("tasks/testTasks/").getPath() + nameTtl, Set.of(DomainRDFFiller.Option.NARY_RELATIONSHIPS_OLD_COMPAT), null);

		situationDomain.validateAndThrow();

		LearningSituation situation = new LearningSituation(situationDomain,
				new HashMap<>(Map.of(
						"answer", new ObjectRef(answer),
						"var", new ObjectRef(var)
				))
		);
		List<DecisionTreeReasoner.DecisionTreeEvaluationResult> branchResultNodes = DecisionTreeReasoner.solve(model.getDecisionTree(), situation);
		Collections.reverse(branchResultNodes);
		String hintText = "";
		for(DecisionTreeReasoner.DecisionTreeEvaluationResult branchResultNode : branchResultNodes) {
			if (!branchResultNode.getNode().getValue() && branchResultNode.getNode().getMetadata().get("alias") != null) {
				break;
			} else if (branchResultNode.getNode().getValue() && branchResultNode.getNode().getMetadata().get("alias") != null) {
				hintText += utilService.generateMessage(branchResultNode.getNode().getMetadata().get("alias").toString(), branchResultNode.getVariablesSnapshot(), situationDomain) + "<br>";
			}
		}
		Assertions.assertEquals(expHint, hintText);
	}

	@ParameterizedTest
	@MethodSource("provideTestErrorTaskCases")
	void testErrorTask(String nameTtl, String var, String answer, String expError) {
		Domain situationDomain = this.model.getDomain().copy();
		DomainRDFFiller.fillDomain(situationDomain, this.getClass().getClassLoader().getResource("tasks/testTasks/").getPath() + nameTtl, Set.of(DomainRDFFiller.Option.NARY_RELATIONSHIPS_OLD_COMPAT), null);

		situationDomain.validateAndThrow();

		LearningSituation situation = new LearningSituation(situationDomain,
				new HashMap<>(Map.of(
						"answer", new ObjectRef(answer),
						"var", new ObjectRef(var)
				))
		);

		List<DecisionTreeReasoner.DecisionTreeEvaluationResult> branchResultNodes = DecisionTreeReasoner.solve(model.getDecisionTree(), situation);
		Collections.reverse(branchResultNodes);
		String errorText = "";
		for(DecisionTreeReasoner.DecisionTreeEvaluationResult branchResultNode : branchResultNodes) {
			if(!branchResultNode.getNode().getValue() && branchResultNode.getNode().getMetadata().get("alias") != null) {
				errorText += utilService.generateMessage(branchResultNode.getNode().getMetadata().get("alias").toString(), branchResultNode.getVariablesSnapshot(), situationDomain) + "<br>";
			}
		}
		Assertions.assertEquals(expError, errorText);
	}
}
