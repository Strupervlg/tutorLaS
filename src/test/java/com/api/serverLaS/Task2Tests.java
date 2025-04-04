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
				Arguments.of("21.ttl", "prefix", "Line6", "Line13", BranchResult.CORRECT),
				Arguments.of("21.ttl", "prefix", "Line6", "Line4", BranchResult.ERROR)
		);
	}

//	static Stream<Arguments> provideTestHintTaskCases() {
//		return Stream.of(
//				Arguments.of("21.ttl", "prefix", "Line6", "Line13", "Свойство с именем \"b\" и по префиксу \"c::d::\" видно в строке 13, так как: имеется такая область видимости, которая является родительской для строки объявления 6 и является родительской для данной строки 13 и через которую можно было бы получить доступ к свойству; имеет модификатор доступа, который виден в данной строке, и не имеет перекрытия.")
//		);
//	}

	static Stream<Arguments> provideTestErrorTaskCases() {
		return Stream.of(
				Arguments.of("21.ttl", "prefix", "Line6", "Line4", "Строка 4 находится до строки 6 объявления свойства, поэтому свойство не может быть видно в ней.<br>")
		);
	}

	static Stream<Arguments> provideCheckForAllTaskCases() {
		return Stream.of(
				Arguments.of("21_forAll1.ttl", "prefix", "Line6", "")
//				Arguments.of("21_forAll2.ttl", "prefix", "Line6", "Свойство с именем \"b\" и по префиксу \"c::d::\" видно в строке 13, так как: имеется такая область видимости, которая является родительской для строки объявления 6 и является родительской для данной строки 13 и через которую можно было бы получить доступ к свойству; имеет модификатор доступа, который виден в данной строке, и не имеет перекрытия.<br>"),
//				Arguments.of("21_forAll3.ttl", "prefix", "Line6", "Свойство с именем \"b\" и по префиксу \"c::d::\" видно в строке 13, так как: имеется такая область видимости, которая является родительской для строки объявления 6 и является родительской для данной строки 13 и через которую можно было бы получить доступ к свойству; имеет модификатор доступа, который виден в данной строке, и не имеет перекрытия.<br>Свойство с именем \"b\" и по префиксу \"c::d::\" видно в строке 12, так как: имеется такая область видимости, которая является родительской для строки объявления 6 и является родительской для данной строки 12 и через которую можно было бы получить доступ к свойству; имеет модификатор доступа, который виден в данной строке, и не имеет перекрытия.<br>")
		);
	}

	@ParameterizedTest
	@MethodSource("provideTestAnswerTaskCases")
	void testAnswerTask(String nameTtl, String prefix, String var, String usageLine, BranchResult expResult) {
		DomainModel situationDomain = this.model.getDomainModel().copy();
		DomainRDFFiller.fillDomain(situationDomain, this.getClass().getClassLoader().getResource("tasks/testTasks/").getPath() + nameTtl, Set.of(DomainRDFFiller.Option.NARY_RELATIONSHIPS_OLD_COMPAT), null);

		situationDomain.validateAndThrow();

		LearningSituation situation = new LearningSituation(situationDomain,
				new HashMap<>(Map.of(
						"usageLine", new ObjectRef(usageLine),
						"var", new ObjectRef(var),
						"prefix", new ObjectRef(prefix)
				))
		);

		DecisionTreeTrace result = DecisionTreeReasoner.solve(model.getDecisionTree(), situation);
		Assertions.assertEquals(expResult, result.getBranchResult());
	}

//	@ParameterizedTest
//	@MethodSource("provideTestHintTaskCases")
//	void testHintTask(String nameTtl, String prefix, String var, String usageLine, String expHint) {
//		DomainModel situationDomain = this.model.getDomainModel().copy();
//		DomainRDFFiller.fillDomain(situationDomain, this.getClass().getClassLoader().getResource("tasks/testTasks/").getPath() + nameTtl, Set.of(DomainRDFFiller.Option.NARY_RELATIONSHIPS_OLD_COMPAT), null);
//
//		situationDomain.validateAndThrow();
//
//		LearningSituation situation = new LearningSituation(situationDomain,
//				new HashMap<>(Map.of(
//						"usageLine", new ObjectRef(usageLine),
//						"var", new ObjectRef(var),
//						"prefix", new ObjectRef(prefix)
//				))
//		);
//
//		BranchResultProcessor resultProcessor = new BranchResultProcessor();
//		DecisionTreeReasoner.solve(model.getDecisionTree(), situation, resultProcessor);
//		String hintText = commonTaskService.generateHintText(resultProcessor.getList(), situationDomain);
//		Assertions.assertEquals(expHint, hintText);
//	}

	@ParameterizedTest
	@MethodSource("provideTestErrorTaskCases")
	void testErrorTask(String nameTtl, String prefix, String var, String usageLine, String expError) {
		DomainModel situationDomain = this.model.getDomainModel().copy();
		DomainRDFFiller.fillDomain(situationDomain, this.getClass().getClassLoader().getResource("tasks/testTasks/").getPath() + nameTtl, Set.of(DomainRDFFiller.Option.NARY_RELATIONSHIPS_OLD_COMPAT), null);

		situationDomain.validateAndThrow();

		LearningSituation situation = new LearningSituation(situationDomain,
				new HashMap<>(Map.of(
						"usageLine", new ObjectRef(usageLine),
						"var", new ObjectRef(var),
						"prefix", new ObjectRef(prefix)
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

	@ParameterizedTest
	@MethodSource("provideCheckForAllTaskCases")
	void testCheckForAllTask(String nameTtl, String prefix, String var, String expError) {
		DomainModel situationDomain = this.model.getDomainModel().copy();
		DomainRDFFiller.fillDomain(situationDomain, this.getClass().getClassLoader().getResource("tasks/testTasks/").getPath() + nameTtl, Set.of(DomainRDFFiller.Option.NARY_RELATIONSHIPS_OLD_COMPAT), null);

		situationDomain.validateAndThrow();

		LearningSituation situation = new LearningSituation(situationDomain,
				new HashMap<>(Map.of(
						"var", new ObjectRef(var),
						"prefix", new ObjectRef(prefix)
				))
		);

		DecisionTreeTrace result = DecisionTreeReasoner.solve(model.getDecisionTrees().get("all"), situation);
		List<DecisionTreeTrace> branchResultNodes = commonTaskService.getListDecisionTreeTrace(result);

		branchResultNodes.sort(
				Comparator.comparingInt(
						(DecisionTreeTrace node) -> {
							if(node.getFinalVariableSnapshot().get("step") != null) {
								return (int) node.getFinalVariableSnapshot().get("step").findIn(situationDomain).getPropertyValue("number", Map.of());
							} else {
								return 0;
							}}));
		String errorText = "";
		for(DecisionTreeTrace branchResultNode : branchResultNodes) {
			if(!branchResultNode.getResultingElement().isAggregated() && branchResultNode.getBranchResult() == BranchResult.ERROR && branchResultNode.getResultingNode().getMetadata().get("alias") != null) {
				errorText += utilService.generateMessage(branchResultNode.getResultingNode().getMetadata().get("alias").toString(), branchResultNode.getFinalVariableSnapshot(), situationDomain) + "<br>";
			}
		}

		Assertions.assertEquals(expError, errorText);
	}
}
