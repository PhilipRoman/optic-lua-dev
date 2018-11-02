package optic.lua.ssa;

import optic.lua.util.*;
import org.antlr.runtime.tree.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.*;

import java.lang.String;
import java.util.*;

import static java.lang.Double.parseDouble;
import static java.util.stream.Collectors.toList;
import static nl.bigo.luaparser.Lua52Walker.Number;
import static nl.bigo.luaparser.Lua52Walker.String;
import static nl.bigo.luaparser.Lua52Walker.*;

/**
 * Functional implementation of a SSA translator. For large inputs (5000 lines of code)
 * peak performance is 15% - 30% lower than {@link MutableFlattener}, unless parallel
 * streams are used, in which case performance is roughly equal.
 */
public class ImmutableFlattener {
	private static final Logger log = LoggerFactory.getLogger(ImmutableFlattener.class);

	public static List<Step> flatten(CommonTree tree) {
		Objects.requireNonNull(tree);
		var statements = Optional.ofNullable(tree.getChildren()).orElse(List.of());
		var f = new ImmutableFlattener();
		return statements.stream()
				.map(Tree.class::cast)
				.map(f::tryFlatten)
				.flatMap(statement -> statement.steps().stream())
				.collect(toList());
	}

	private FlatVoid tryFlatten(Tree tree) {
		try {
			return flattenStatement(tree).prependComment("line " + tree.getLine());
		} catch (RuntimeException e) {
			String msg = java.lang.String.format(
					"Error at line %d while flattening %s",
					tree.getLine(),
					tree.toString());
			log.error(msg, e);
			return FlatVoid.start().prependComment("Error: " + e);
		}
	}

	@NotNull
	private FlatVoid flattenStatement(Tree t) {
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
				FlatValue from = flattenExpression(t.getChild(1));
				FlatValue to = flattenExpression(t.getChild(2));
				CommonTree block = (CommonTree) t.getChild(3).getChild(0);
				List<Step> body = flatten(block);
				return from
						.and(to.steps())
						.and(StepFactory.forRange(varName, from.result(), to.result(), body));
			}
			case Do: {
				CommonTree block = (CommonTree) t;
				return FlatVoid.start()
						.and(StepFactory.doBlock(flatten(block)));
			}
			case CHUNK: {
				CommonTree block = (CommonTree) t;
				return FlatVoid.start()
						.and(flatten(block));
			}
			case Return: {
				List<FlatValue> values = ((CommonTree) t).getChildren().stream()
						.map(Tree.class::cast)
						.map(this::flattenExpression)
						.collect(toList());
				List<Step> steps = values.stream()
						.map(FlatValue::steps)
						.flatMap(List::stream)
						.collect(toList());
				List<Register> registers = values.stream()
						.map(FlatValue::result)
						.collect(toList());
				return FlatVoid.start()
						.and(steps)
						.and(StepFactory.returnFromFunction(registers));
			}
		}
		throw new RuntimeException("Unknown statement: " + t);
	}

	private FlatValue flattenExpression(Tree t) {
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
			FlatValue param = flattenExpression(t.getChild(0));
			return param
					.and(StepFactory.unaryOperator(param.result(), op, register))
					.resultWillBeIn(register);
		}
		switch (t.getType()) {
			case Number: {
				var register = Register.create();
				double value = parseDouble(t.getText());
				return FlatValue.createExpression(
						List.of(StepFactory.constNumber(register, value)),
						register
				);
			}
			case String: {
				var register = Register.create();
				String value = (t.getText());
				return FlatValue.createExpression(
						List.of(StepFactory.constString(register, value)),
						register
				);
			}
			case Name: {
				var register = Register.create();
				String name = t.getText();
				return FlatValue.createExpression(
						List.of(StepFactory.dereference(register, name)),
						register
				);
			}
			case VAR: {
				return (FlatValue) createFunctionCall(t, true);
			}
			case FUNCTION: {
				return createFunctionLiteral(t);
			}
			case TABLE: {
				return createTableLiteral(t);
			}
			case DotDotDot: {
				var reg = Register.createVararg();
				return FlatValue.start()
						.and(StepFactory.getVarargs(reg))
						.resultWillBeIn(reg);
			}
		}
		throw new RuntimeException("Unknown expression: " + t);
	}

	private FlatValue createTableLiteral(Tree tree) {
		TreeTypes.expect(TABLE, tree);
		int index = 1;
		List<Step> steps = new ArrayList<>(tree.getChildCount() * 3);
		Map<Register, Register> table = new HashMap<>();
		for (Object obj : ((CommonTree) tree).getChildren()) {
			var child = TreeTypes.expect(FIELD, (CommonTree) obj);
			boolean hasKey = child.getChildCount() == 2;
			if (!hasKey && child.getChildCount() != 1) throw new AssertionError();
			if (hasKey) {
				var key = flattenExpression(child.getChild(0));
				steps.addAll(key.steps());
				var value = flattenExpression(child.getChild(1));
				steps.addAll(value.steps());
				table.put(key.result(), value.result());
			} else {
				int key = index++;
				Register keyRegister = Register.create();
				steps.add(StepFactory.constNumber(keyRegister, key));
				var value = flattenExpression(child.getChild(0));
				steps.addAll(value.steps());
				table.put(keyRegister, value.result());
			}
		}
		Register result = Register.create();
		var makeTable = StepFactory.createTable(table, result);
		return FlatVoid.start()
				.and(steps)
				.and(makeTable)
				.resultWillBeIn(result);
	}

	private FlatVoid createAssignment(CommonTree t, boolean local) {
		var nameList = TreeTypes.expect(local ? NAME_LIST : VAR_LIST, t.getChild(0));
		List<String> names = ((CommonTree) nameList).getChildren().stream()
				.map(Object::toString)
				.collect(toList());
		var valueList = TreeTypes.expect(EXPR_LIST, t.getChild(1));
		List<FlatValue> expressions = ((CommonTree) valueList).getChildren().stream()
				.map(Tree.class::cast)
				.map(this::flattenExpression)
				.collect(toList());
		List<Step> evaluateExpressions = expressions.stream()
				.flatMap(e -> e.steps().stream())
				.collect(toList());
		List<Register> registers = expressions.stream()
				.map(FlatValue::result)
				.collect(toList());
		List<Step> declareLocals = names.stream()
				.map(StepFactory::declareLocal)
				.collect(toList());
		var result = FlatVoid.start();
		if (local) {
			result = result.and(declareLocals);
		}
		return result.and(evaluateExpressions)
				.and(StepFactory.assign(names, registers));
	}

	@NotNull
	private FlatVoid createFunctionCall(Tree t, boolean expression) {
		var lookupFunction = flattenExpression(t.getChild(0));
		Register function = lookupFunction.result();
		var call = TreeTypes.expect(CALL, t.getChild(1));
		var args = ((CommonTree) call).getChildren();
		args = Objects.requireNonNullElse(args, List.of());
		List<FlatValue> argExpressions = args.stream()
				.map(Tree.class::cast)
				.map(this::flattenExpression)
				.collect(toList());
		List<Register> argRegisters = argExpressions.stream()
				.map(FlatValue::result)
				.collect(toList());
		List<Step> evaluateArgs = argExpressions.stream()
				.flatMap(expr -> expr.steps().stream())
				.collect(toList());
		if (expression) {
			Register register = Register.createVararg();
			return FlatVoid.start()
					.and(lookupFunction.steps())
					.and(evaluateArgs)
					.and(StepFactory.call(function, argRegisters, register))
					.resultWillBeIn(register);
		} else {
			return FlatVoid.start()
					.and(lookupFunction.steps())
					.and(evaluateArgs)
					.and(StepFactory.call(function, argRegisters));
		}
	}

	private FlatValue createFunctionLiteral(Tree t) {
		TreeTypes.expect(FUNCTION, t);
		Tree chunk = TreeTypes.expect(CHUNK, t.getChild(1));
		List<Step> body = flattenStatement(chunk).steps();
		Tree paramList = TreeTypes.expect(PARAM_LIST, t.getChild(0));
		var params = ParameterList.parse(((CommonTree) paramList));
		Register out = Register.create();
		Step makeFunc = StepFactory.functionLiteral(body, out, params);
		return FlatValue.createExpression(List.of(makeFunc), out);
	}
}
