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
class Task2Tests {

	@Autowired
	private CommonTaskService commonTaskService;

	@Autowired
	private UtilService utilService;

	public DomainSolvingModel model = new DomainSolvingModel(
			this.getClass().getClassLoader().getResource("Task2/"),
			DomainSolvingModel.BuildMethod.DICT_RDF
	);

	static Stream<Arguments> provideTestAnswerTaskCases() {
		return Stream.of(
				Arguments.of("21.ttl", "prefix", "Line6", "Line13", true),
				Arguments.of("21.ttl", "prefix", "Line6", "Line4", false)
		);
	}

	static Stream<Arguments> provideTestHintTaskCases() {
		return Stream.of(
				Arguments.of("21.ttl", "prefix", "Line6", "Line13", "Переменная с именем \"b\" и по префиксу \"c::d::\" видна в строке 13 и не имеет перекрытия.")
				//TODO еще одну если есть глобал префикс
		);
	}

	static Stream<Arguments> provideTestErrorTaskCases() {
		return Stream.of(
				Arguments.of("21.ttl", "prefix", "Line6", "Line4", "Строка 4 находится до строки 6 объявления переменной, поэтому переменная не может быть видна в ней.<br>")
		);
	}

	static Stream<Arguments> provideCheckForAllTaskCases() {
		return Stream.of(
				Arguments.of("21_forAll1.ttl", "prefix", "Line6", ""),
				Arguments.of("21_forAll2.ttl", "prefix", "Line6", "Переменная с именем \"b\" и по префиксу \"c::d::\" видна в строке 13.<br>"),
				Arguments.of("21_forAll3.ttl", "prefix", "Line6", "Переменная с именем \"b\" и по префиксу \"c::d::\" видна в строке 13.<br>Переменная с именем \"b\" и по префиксу \"c::d::\" видна в строке 12.<br>")
		);
	}

	@ParameterizedTest
	@MethodSource("provideTestAnswerTaskCases")
	void testAnswerTask(String nameTtl, String prefix, String var, String usageLine, boolean expResult) {
		Domain situationDomain = this.model.getDomain().copy();
		DomainRDFFiller.fillDomain(situationDomain, this.getClass().getClassLoader().getResource("tasks/testTasks/").getPath() + nameTtl, Set.of(DomainRDFFiller.Option.NARY_RELATIONSHIPS_OLD_COMPAT), null);

		situationDomain.validateAndThrow();

		LearningSituation situation = new LearningSituation(situationDomain,
				new HashMap<>(Map.of(
						"usageLine", new ObjectRef(usageLine),
						"var", new ObjectRef(var),
						"prefix", new ObjectRef(prefix)
				))
		);

		boolean branchResultNodes = DecisionTreeReasoner.getAnswer(model.getDecisionTree().getMainBranch(), situation);
		Assertions.assertEquals(expResult, branchResultNodes);
	}

	@ParameterizedTest
	@MethodSource("provideTestHintTaskCases")
	void testHintTask(String nameTtl, String prefix, String var, String usageLine, String expHint) {
		Domain situationDomain = this.model.getDomain().copy();
		DomainRDFFiller.fillDomain(situationDomain, this.getClass().getClassLoader().getResource("tasks/testTasks/").getPath() + nameTtl, Set.of(DomainRDFFiller.Option.NARY_RELATIONSHIPS_OLD_COMPAT), null);

		situationDomain.validateAndThrow();

		LearningSituation situation = new LearningSituation(situationDomain,
				new HashMap<>(Map.of(
						"usageLine", new ObjectRef(usageLine),
						"var", new ObjectRef(var),
						"prefix", new ObjectRef(prefix)
				))
		);

		List<DecisionTreeReasoner.DecisionTreeEvaluationResult> branchResultNodes = DecisionTreeReasoner.solve(model.getDecisionTree(), situation);
		String hintText = commonTaskService.generateHintText(branchResultNodes, situationDomain);
		Assertions.assertEquals(expHint, hintText);
	}

	@ParameterizedTest
	@MethodSource("provideTestErrorTaskCases")
	void testErrorTask(String nameTtl, String prefix, String var, String usageLine, String expError) {
		Domain situationDomain = this.model.getDomain().copy();
		DomainRDFFiller.fillDomain(situationDomain, this.getClass().getClassLoader().getResource("tasks/testTasks/").getPath() + nameTtl, Set.of(DomainRDFFiller.Option.NARY_RELATIONSHIPS_OLD_COMPAT), null);

		situationDomain.validateAndThrow();

		LearningSituation situation = new LearningSituation(situationDomain,
				new HashMap<>(Map.of(
						"usageLine", new ObjectRef(usageLine),
						"var", new ObjectRef(var),
						"prefix", new ObjectRef(prefix)
				))
		);

		List<DecisionTreeReasoner.DecisionTreeEvaluationResult> branchResultNodes = DecisionTreeReasoner.solve(model.getDecisionTree(), situation);
		String errorText = "";
		for(DecisionTreeReasoner.DecisionTreeEvaluationResult branchResultNode : branchResultNodes) {
			if(!branchResultNode.getNode().getValue() && branchResultNode.getNode().getMetadata().get("alias") != null) {
				errorText += utilService.generateMessage(branchResultNode.getNode().getMetadata().get("alias").toString(), branchResultNode.getVariablesSnapshot(), situationDomain) + "<br>";
			}
		}
		Assertions.assertEquals(expError, errorText);
	}

	@ParameterizedTest
	@MethodSource("provideCheckForAllTaskCases")
	void testCheckForAllTask(String nameTtl, String prefix, String var, String expError) {
		Domain situationDomain = this.model.getDomain().copy();
		DomainRDFFiller.fillDomain(situationDomain, this.getClass().getClassLoader().getResource("tasks/testTasks/").getPath() + nameTtl, Set.of(DomainRDFFiller.Option.NARY_RELATIONSHIPS_OLD_COMPAT), null);

		situationDomain.validateAndThrow();

		LearningSituation situation = new LearningSituation(situationDomain,
				new HashMap<>(Map.of(
						"var", new ObjectRef(var),
						"prefix", new ObjectRef(prefix)
				))
		);

		List<DecisionTreeReasoner.DecisionTreeEvaluationResult> branchResultNodes = DecisionTreeReasoner.solve(model.getDecisionTrees().get("all"), situation);
		branchResultNodes.sort(
				Comparator.comparingInt(
						(DecisionTreeReasoner.DecisionTreeEvaluationResult node) -> {
							if(node.getVariablesSnapshot().get("step") != null) {
								return (int) node.getVariablesSnapshot().get("step").findIn(situationDomain).getPropertyValue("number");
							} else {
								return 0;
							}}));
		String errorText = "";
		for(DecisionTreeReasoner.DecisionTreeEvaluationResult branchResultNode : branchResultNodes) {
			if(!branchResultNode.getNode().getValue() && branchResultNode.getNode().getMetadata().get("alias") != null) {
				errorText += utilService.generateMessage(branchResultNode.getNode().getMetadata().get("alias").toString(), branchResultNode.getVariablesSnapshot(), situationDomain) + "<br>";
			}
		}

		Assertions.assertEquals(expError, errorText);
	}
}
