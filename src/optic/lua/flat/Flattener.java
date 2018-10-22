package optic.lua.flat;

import optic.lua.util.*;
import org.antlr.runtime.tree.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.*;

import java.lang.String;
import java.util.*;

import static java.lang.Double.parseDouble;
import static java.util.stream.Collectors.*;
import static nl.bigo.luaparser.Lua52Walker.*;

public class Flattener implements TreeScheduler {
	private static final Logger log = LoggerFactory.getLogger(Flattener.class);

	@Override
	public List<Step> schedule(CommonTree tree) {
		return flatten(tree);
	}

	public List<Step> flatten(CommonTree tree) {
		Objects.requireNonNull(tree);
		return tree.getChildren().stream()
				.map(Tree.class::cast)
				.map(t -> flattenStatement(t).prependComment("line " + t.getLine()))
				.flatMap(statement -> statement.steps().stream())
				.collect(toList());
	}

	@NotNull
	private FlatStatement flattenStatement(Tree t) {
		Objects.requireNonNull(t);
		switch (t.getType()) {
			// watch for fall-through behaviour!
			case ASSIGNMENT: {
				return createAssignment((CommonTree) t, false);
			}
			case LOCAL_ASSIGNMENT: {
				return createAssignment((CommonTree) t, true);
			}
			case VAR: {
				return createFunctionCall(t, false);
			}
			case For: {
				String varName = t.getChild(0).toString();
				FlatExpression from = flattenExpression(t.getChild(1));
				FlatExpression to = flattenExpression(t.getChild(2));
				CommonTree block = (CommonTree) t.getChild(3).getChild(0);
				List<Step> body = flatten(block);
				return from
						.and(to.steps())
						.and(StepFactory.forRange(varName, from.result(), to.result(), body));
			}
		}
		throw new RuntimeException("Unknown statement: " + t);
	}

	private FlatExpression flattenExpression(Tree t) {
		Objects.requireNonNull(t);
		if (Operators.isBinary(t)) {
			var register = new Register();
			var a = flattenExpression(t.getChild(0));
			var b = flattenExpression(t.getChild(1));
			String op = t.getText();
			var step = StepFactory.binaryOperator(a.result(), b.result(), op, register);
			return a.and(b.steps())
					.and(step)
					.putResultIn(register);
		}
		if (Operators.isUnary(t)) {
			var register = new Register();
			String op = Operators.getUnarySymbol(t);
			FlatExpression param = flattenExpression(t.getChild(0));
			return param
					.and(StepFactory.unaryOperator(param.result(), op, register))
					.putResultIn(register);
		}
		switch (t.getType()) {
			case Number: {
				var register = new Register();
				double value = parseDouble(t.getText());
				return FlatExpression.createExpression(
						List.of(StepFactory.constNumber(register, value)),
						register
				);
			}
			case String: {
				var register = new Register();
				String value = (t.getText());
				return FlatExpression.createExpression(
						List.of(StepFactory.constString(register, value)),
						register
				);
			}
			case Name: {
				var register = new Register();
				String name = t.getText();
				return FlatExpression.createExpression(
						List.of(StepFactory.dereference(register, name)),
						register
				);
			}
			case VAR: {
				return (FlatExpression) createFunctionCall(t, true);
			}
		}
		throw new RuntimeException("Unknown expression: " + t);
	}

	private FlatStatement createAssignment(CommonTree t, boolean local) {
		var nameList = TreeTypes.expect(local ? NAME_LIST : VAR_LIST, t.getChild(0));
		List<String> names = ((CommonTree) nameList).getChildren().stream()
				.map(Object::toString)
				.collect(toList());
		var valueList = TreeTypes.expect(EXPR_LIST, t.getChild(1));
		List<FlatExpression> expressions = ((CommonTree) valueList).getChildren().stream()
				.map(Tree.class::cast)
				.map(this::flattenExpression)
				.collect(toList());
		List<Step> evaluateExpressions = expressions.stream()
				.flatMap(e -> e.steps().stream())
				.collect(toList());
		List<Register> registers = expressions.stream()
				.map(FlatExpression::result)
				.collect(toList());
		List<Step> declareLocals = names.stream()
				.map(StepFactory::declareLocal)
				.collect(toList());
		var result = FlatStatement.emptyStatement();
		if (local) {
			result = result.and(declareLocals);
		}
		return result.and(evaluateExpressions)
				.and(StepFactory.assign(names, registers));
	}

	@NotNull
	private FlatStatement createFunctionCall(Tree t, boolean expression) {
		var lookupFunction = flattenExpression(t.getChild(0));
		Register function = lookupFunction.result();
		var call = TreeTypes.expect(CALL, t.getChild(1));
		var args = ((CommonTree) call).getChildren();
		List<FlatExpression> argExpressions = args.stream()
				.map(Tree.class::cast)
				.map(this::flattenExpression)
				.collect(toList());
		List<Register> argRegisters = argExpressions.stream()
				.map(FlatExpression::result)
				.collect(toList());
		List<Step> evaluateArgs = argExpressions.stream()
				.flatMap(expr -> expr.steps().stream())
				.collect(toList());
		if (expression) {
			Register register = Register.multiValued();
			return FlatStatement.emptyStatement()
					.and(lookupFunction.steps())
					.and(evaluateArgs)
					.and(StepFactory.call(function, argRegisters, register))
					.putResultIn(register);
		} else {
			return FlatStatement.emptyStatement()
					.and(lookupFunction.steps())
					.and(evaluateArgs)
					.and(StepFactory.call(function, argRegisters));
		}
	}
}
