package com.api.serverLaS;

import com.api.serverLaS.services.CommonTaskService;
import com.api.serverLaS.services.UtilService;
import its.model.DomainSolvingModel;
import its.model.definition.DomainModel;
import its.model.definition.ObjectRef;
import its.model.definition.rdf.DomainRDFFiller;
import its.model.nodes.BranchResult;
import its.model.nodes.BranchResultNode;
import its.reasoner.BranchResultProcessor;
import its.reasoner.LearningSituation;
import its.reasoner.nodes.DecisionTreeEvaluationResult;
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
class Task1Tests {

	@Autowired
	private CommonTaskService commonTaskService;

	@Autowired
	private UtilService utilService;

	public DomainSolvingModel model = new DomainSolvingModel(
			this.getClass().getClassLoader().getResource("Task1/"),
			DomainSolvingModel.BuildMethod.DICT_RDF
	);

	static Stream<Arguments> provideTestAnswerTaskCases() {
		return Stream.of(
				Arguments.of("11.ttl", "step3", "step4", "VariableA", BranchResult.CORRECT),
				Arguments.of("11.ttl", "step3", "step2", "VariableA", BranchResult.ERROR),
				Arguments.of("11.ttl", "step3", "step21", "VariableA", BranchResult.ERROR),
				Arguments.of("12.ttl", "step4", "step2", "VariableA", BranchResult.CORRECT)
		);
	}

	static Stream<Arguments> provideTestHintTaskCases() {
		return Stream.of(
				Arguments.of("11.ttl", "step3", "step4", "VariableA", "В 4 строке трассы переменная \"a\" существует, так как находится внутри выполнения функции main, в которой находится строка создания переменной."),
				Arguments.of("12.ttl", "step4", "step2", "VariableA", "В 2 строке трассы переменная \"a\" существует, так как является глобальной и статической и существует в течение всего времени выполнения программы. ")
		);
	}

	static Stream<Arguments> provideTestErrorTaskCases() {
		return Stream.of(
				Arguments.of("11.ttl", "step3", "step2", "VariableA", "Выбранная строка трассы находится до создания переменной \"a\", поэтому в этой строке переменная еще не существует.<br>"),
				Arguments.of("11.ttl", "step3", "step21", "VariableA", "В выбранной строке трассы переменная \"a\" не существует, так как строка находится за пределом выполнения функции main, в которой создается переменная.<br>")
		);
	}

	static Stream<Arguments> provideCheckForAllTaskCases() {
		return Stream.of(
				Arguments.of("11_forAll1.ttl", "step3", "VariableA", ""),
				Arguments.of("11_forAll2.ttl", "step3", "VariableA", "В 15 строке трассы переменная \"a\" существует, так как находится внутри функции main, что и строка создания переменной.<br>"),
				Arguments.of("11_forAll3.ttl", "step3", "VariableA", "В 5 строке трассы переменная \"a\" существует, так как находится внутри функции main, что и строка создания переменной.<br>В 15 строке трассы переменная \"a\" существует, так как находится внутри функции main, что и строка создания переменной.<br>"),
				Arguments.of("12_forAll1.ttl", "step4", "VariableA", "В 2 строке трассы переменная \"a\" существует, так как является глобальной и существует в течение всего времени выполнения программы.<br>В 2 строке трассы переменная \"a\" существует, так как является статической и существует в течение всего времени выполнения программы.<br>"),
				Arguments.of("12_forAll2.ttl", "step4", "VariableA", "В 2 строке трассы переменная \"a\" существует, так как является глобальной и существует в течение всего времени выполнения программы.<br>"),
				Arguments.of("12_forAll3.ttl", "step4", "VariableA", "В 2 строке трассы переменная \"a\" существует, так как является глобальной и существует в течение всего времени выполнения программы.<br>В 8 строке трассы переменная \"a\" существует, так как является глобальной и существует в течение всего времени выполнения программы.<br>")
		);
	}

	@ParameterizedTest
	@MethodSource("provideTestAnswerTaskCases")
	void testAnswerTask(String nameTtl, String stepVar, String step, String var, BranchResult expResult) {
		DomainModel situationDomain = this.model.getDomainModel().copy();
		DomainRDFFiller.fillDomain(situationDomain, this.getClass().getClassLoader().getResource("tasks/testTasks/").getPath() + nameTtl, Set.of(DomainRDFFiller.Option.NARY_RELATIONSHIPS_OLD_COMPAT), null);

		situationDomain.validateAndThrow();

		LearningSituation situation = new LearningSituation(situationDomain,
				new HashMap<>(Map.of(
						"stepVar", new ObjectRef(stepVar),
						"step", new ObjectRef(step),
						"var", new ObjectRef(var)
				))
		);

		DecisionTreeEvaluationResult<?> result = DecisionTreeReasoner.solve(model.getDecisionTree(), situation);
		Assertions.assertEquals(expResult, result.getValue());
	}

	@ParameterizedTest
	@MethodSource("provideTestHintTaskCases")
	void testHintTask(String nameTtl, String stepVar, String step, String var, String expHint) {
		DomainModel situationDomain = this.model.getDomainModel().copy();
		DomainRDFFiller.fillDomain(situationDomain, this.getClass().getClassLoader().getResource("tasks/testTasks/").getPath() + nameTtl, Set.of(DomainRDFFiller.Option.NARY_RELATIONSHIPS_OLD_COMPAT), null);

		situationDomain.validateAndThrow();

		LearningSituation situation = new LearningSituation(situationDomain,
				new HashMap<>(Map.of(
						"stepVar", new ObjectRef(stepVar),
						"step", new ObjectRef(step),
						"var", new ObjectRef(var)
				))
		);

		BranchResultProcessor resultProcessor = new BranchResultProcessor();
		DecisionTreeReasoner.solve(model.getDecisionTree(), situation, resultProcessor);
		String hintText = commonTaskService.generateHintText(resultProcessor.getList(), situationDomain);
		Assertions.assertEquals(expHint, hintText);
	}

	@ParameterizedTest
	@MethodSource("provideTestErrorTaskCases")
	void testErrorTask(String nameTtl, String stepVar, String step, String var, String expError) {
		DomainModel situationDomain = this.model.getDomainModel().copy();
		DomainRDFFiller.fillDomain(situationDomain, this.getClass().getClassLoader().getResource("tasks/testTasks/").getPath() + nameTtl, Set.of(DomainRDFFiller.Option.NARY_RELATIONSHIPS_OLD_COMPAT), null);

		situationDomain.validateAndThrow();

		LearningSituation situation = new LearningSituation(situationDomain,
				new HashMap<>(Map.of(
						"stepVar", new ObjectRef(stepVar),
						"step", new ObjectRef(step),
						"var", new ObjectRef(var)
				))
		);

		BranchResultProcessor resultProcessor = new BranchResultProcessor();
		DecisionTreeReasoner.solve(model.getDecisionTree(), situation, resultProcessor);
		String errorText = "";
		for(DecisionTreeEvaluationResult<BranchResultNode> branchResultNode : resultProcessor.getList()) {
			if(branchResultNode.getNode().getValue() == BranchResult.ERROR && branchResultNode.getNode().getMetadata().get("alias") != null) {
				errorText += utilService.generateMessage(branchResultNode.getNode().getMetadata().get("alias").toString(), branchResultNode.getVariablesSnapshot(), situationDomain) + "<br>";
			}
		}
		Assertions.assertEquals(expError, errorText);
	}

	@ParameterizedTest
	@MethodSource("provideCheckForAllTaskCases")
	void testCheckForAllTask(String nameTtl, String stepVar, String var, String expError) {
		DomainModel situationDomain = this.model.getDomainModel().copy();
		DomainRDFFiller.fillDomain(situationDomain, this.getClass().getClassLoader().getResource("tasks/testTasks/").getPath() + nameTtl, Set.of(DomainRDFFiller.Option.NARY_RELATIONSHIPS_OLD_COMPAT), null);

		situationDomain.validateAndThrow();

		LearningSituation situation = new LearningSituation(situationDomain,
				new HashMap<>(Map.of(
						"stepVar", new ObjectRef(stepVar),
						"var", new ObjectRef(var)
				))
		);

		BranchResultProcessor resultProcessor = new BranchResultProcessor();
		DecisionTreeReasoner.solve(model.getDecisionTrees().get("all"), situation, resultProcessor);
		List<DecisionTreeEvaluationResult<BranchResultNode>> branchResultNodes = resultProcessor.getList();
		branchResultNodes.sort(
				Comparator.comparingInt(
						(DecisionTreeEvaluationResult<BranchResultNode> node) -> {
							if(node.getVariablesSnapshot().get("step") != null) {
								return (int) node.getVariablesSnapshot().get("step").findIn(situationDomain).getPropertyValue("number");
							} else {
								return 0;
							}}));
		String errorText = "";
		for(DecisionTreeEvaluationResult<BranchResultNode> branchResultNode : branchResultNodes) {
			if(branchResultNode.getValue() == BranchResult.ERROR && branchResultNode.getNode().getMetadata().get("alias") != null) {

				errorText += utilService.generateMessage(branchResultNode.getNode().getMetadata().get("alias").toString(), branchResultNode.getVariablesSnapshot(), situationDomain) + "<br>";
			}
		}

		Assertions.assertEquals(expError, errorText);
	}
}
