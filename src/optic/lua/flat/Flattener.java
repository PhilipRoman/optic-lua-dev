package optic.lua.flat;

import optic.lua.util.*;
import org.antlr.runtime.tree.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.*;

import java.lang.String;
import java.util.*;

import static java.lang.Double.*;
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
		var statements = Optional.ofNullable(tree.getChildren()).orElse(List.of());
		return statements.stream()
				.map(Tree.class::cast)
				.map(this::tryFlatten)
				.flatMap(statement -> statement.steps().stream())
				.collect(toList());
	}

	private FlatStatement tryFlatten(Tree tree) {
		try {
			return flattenStatement(tree).prependComment("line " + tree.getLine());
		} catch (RuntimeException e) {
			String msg = java.lang.String.format(
					"Error at line %d while flattening %s",
					tree.getLine(),
					tree.toString());
			log.error(msg, e);
			return FlatStatement.start().prependComment("Error: " + e);
		}
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
			case Do: {
				CommonTree block = (CommonTree) t;
				return FlatStatement.start()
						.and(StepFactory.doBlock(flatten(block)));
			}
			case CHUNK: {
				CommonTree block = (CommonTree) t;
				return FlatStatement.start()
						.and(flatten(block));
			}
			case Return: {
				List<FlatExpression> values = ((CommonTree) t).getChildren().stream()
						.map(Tree.class::cast)
						.map(this::flattenExpression)
						.collect(toList());
				List<Step> steps = values.stream()
						.map(FlatExpression::steps)
						.flatMap(List::stream)
						.collect(toList());
				List<Register> registers = values.stream()
						.map(FlatExpression::result)
						.collect(toList());
				return FlatStatement.start()
						.and(steps)
						.and(StepFactory.returnFromFunction(registers));
			}
		}
		throw new RuntimeException("Unknown statement: " + t);
	}

	private FlatExpression flattenExpression(Tree t) {
		Objects.requireNonNull(t);
		if (Operators.isBinary(t)) {
			var register = Register.create();
			var a = flattenExpression(t.getChild(0));
			var b = flattenExpression(t.getChild(1));
			String op = t.getText();
			var step = StepFactory.binaryOperator(a.result(), b.result(), op, register);
			return a.and(b.steps())
					.and(step)
					.resultWillBeIn(register);
		}
		if (Operators.isUnary(t)) {
			var register = Register.create();
			String op = Operators.getUnarySymbol(t);
			FlatExpression param = flattenExpression(t.getChild(0));
			return param
					.and(StepFactory.unaryOperator(param.result(), op, register))
					.resultWillBeIn(register);
		}
		switch (t.getType()) {
			case Number: {
				var register = Register.create();
				double value = parseDouble(t.getText());
				return FlatExpression.createExpression(
						List.of(StepFactory.constNumber(register, value)),
						register
				);
			}
			case String: {
				var register = Register.create();
				String value = (t.getText());
				return FlatExpression.createExpression(
						List.of(StepFactory.constString(register, value)),
						register
				);
			}
			case Name: {
				var register = Register.create();
				String name = t.getText();
				return FlatExpression.createExpression(
						List.of(StepFactory.dereference(register, name)),
						register
				);
			}
			case VAR: {
				return (FlatExpression) createFunctionCall(t, true);
			}
			case FUNCTION: {
				return createFunctionLiteral(t);
			}
			case TABLE: {
				return createTableLiteral(t);
			}
			case DotDotDot: {
				var reg = Register.createVararg();
				return FlatExpression.start()
						.and(StepFactory.getVarargs(reg))
						.resultWillBeIn(reg);
			}
		}
		throw new RuntimeException("Unknown expression: " + t);
	}

	private FlatExpression createTableLiteral(Tree tree) {
		return FlatStatement.start().resultWillBeIn(Register.create());
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
		var result = FlatStatement.start();
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
		args = Objects.requireNonNullElse(args, List.of());
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
			Register register = Register.createVararg();
			return FlatStatement.start()
					.and(lookupFunction.steps())
					.and(evaluateArgs)
					.and(StepFactory.call(function, argRegisters, register))
					.resultWillBeIn(register);
		} else {
			return FlatStatement.start()
					.and(lookupFunction.steps())
					.and(evaluateArgs)
					.and(StepFactory.call(function, argRegisters));
		}
	}

	private FlatExpression createFunctionLiteral(Tree t) {
		TreeTypes.expect(FUNCTION, t);
		Tree chunk = TreeTypes.expect(CHUNK, t.getChild(1));
		List<Step> body = flattenStatement(chunk).steps();
		Tree paramList = TreeTypes.expect(PARAM_LIST, t.getChild(0));
		var params = ParameterList.parse(((CommonTree) paramList));
		Register out = Register.create();
		Step makeFunc = StepFactory.functionLiteral(body, out, params);
		return FlatExpression.createExpression(List.of(makeFunc), out);
	}
}
