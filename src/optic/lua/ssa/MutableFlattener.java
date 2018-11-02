package optic.lua.ssa;

import optic.lua.util.*;
import org.antlr.runtime.tree.*;
import org.jetbrains.annotations.*;
import org.slf4j.*;

import java.lang.String;
import java.util.*;

import static java.lang.Double.parseDouble;
import static java.util.stream.Collectors.toList;
import static nl.bigo.luaparser.Lua52Walker.Number;
import static nl.bigo.luaparser.Lua52Walker.String;
import static nl.bigo.luaparser.Lua52Walker.*;

/**
 * Mutable implementation of SSA translator. Good startup performance (can be 4x faster on first iteration).
 * Need to investigate parallelism capabilities.
 */
public class MutableFlattener {
	private static final Logger log = LoggerFactory.getLogger(MutableFlattener.class);
	private final List<Step> steps;

	private MutableFlattener(List<Step> steps) {
		Objects.requireNonNull(steps);
		this.steps = steps;
	}

	public static List<Step> flatten(CommonTree tree) {
		Objects.requireNonNull(tree);
		var statements = Optional.ofNullable(tree.getChildren()).orElse(List.of());
		int expectedSize = statements.size() * 4;
		var f = new MutableFlattener(new ArrayList<>(expectedSize));
		for (var stat : statements) {
			f.tryFlatten((Tree) stat);
		}
		int actualSize = f.steps.size();
		log.debug("Expected size: {}, actual size: {}", expectedSize, actualSize);
		return f.steps;
	}

	private void tryFlatten(Tree tree) {
		try {
			steps.add(StepFactory.comment("line " + tree.getLine()));
			flattenStatement(tree);
		} catch (RuntimeException e) {
			String msg = java.lang.String.format(
					"Error at line %d while flattening %s",
					tree.getLine(),
					tree.toString());
			log.error(msg, e);
			log.error("Flattener state: {}", steps);
			steps.add(StepFactory.comment("Error: " + e));
		}
	}

	private void flattenStatement(Tree t) {
		Objects.requireNonNull(t);
		switch (t.getType()) {
			// watch for fall-through behaviour!
			case ASSIGNMENT: {
				createAssignment((CommonTree) t, false);
				return;
			}
			case LOCAL_ASSIGNMENT: {
				createAssignment((CommonTree) t, true);
				return;
			}
			case VAR: {
				createFunctionCall(t, false);
				return;
			}
			case For: {
				String varName = t.getChild(0).toString();
				Register from = flattenExpression(t.getChild(1));
				Register to = flattenExpression(t.getChild(2));
				CommonTree block = (CommonTree) t.getChild(3).getChild(0);
				List<Step> body = flatten(block);
				steps.add(StepFactory.forRange(varName, from, to, body));
				return;
			}
			case Do: {
				CommonTree block = (CommonTree) t;
				steps.add(StepFactory.doBlock(flatten(block)));
				return;
			}
			case CHUNK: {
				CommonTree block = (CommonTree) t;
				steps.addAll(flatten(block));
				return;
			}
			case Return: {
				List<Register> registers = new ArrayList<>(t.getChildCount());
				for (Object o : ((CommonTree) t).getChildren()) {
					Tree tree = (Tree) o;
					Register register = flattenExpression(tree);
					registers.add(register);
				}
				steps.add(StepFactory.returnFromFunction(registers));
				return;
			}
		}
		throw new RuntimeException("Unknown statement: " + t);
	}

	private Register flattenExpression(Tree t) {
		Objects.requireNonNull(t);
		if (Operators.isBinary(t)) {
			var register = Register.create();
			var a = flattenExpression(t.getChild(0));
			var b = flattenExpression(t.getChild(1));
			String op = t.getText();
			steps.add(StepFactory.binaryOperator(a, b, op, register));
			return register;
		}
		if (Operators.isUnary(t)) {
			var register = Register.create();
			String op = Operators.getUnarySymbol(t);
			Register param = flattenExpression(t.getChild(0));
			steps.add(StepFactory.unaryOperator(param, op, register));
			return register;
		}
		switch (t.getType()) {
			case Number: {
				var register = Register.create();
				double value = parseDouble(t.getText());
				steps.add(StepFactory.constNumber(register, value));
				return register;
			}
			case String: {
				var register = Register.create();
				String value = (t.getText());
				steps.add(StepFactory.constString(register, value));
				return register;
			}
			case Name: {
				var register = Register.create();
				String name = t.getText();
				steps.add(StepFactory.dereference(register, name));
				return register;
			}
			case VAR: {
				return createFunctionCall(t, true);
			}
			case FUNCTION: {
				return createFunctionLiteral(t);
			}
			case TABLE: {
				return createTableLiteral(t);
			}
			case DotDotDot: {
				var reg = Register.createVararg();
				steps.add(StepFactory.getVarargs(reg));
				return reg;
			}
		}
		throw new RuntimeException("Unknown expression: " + t);
	}

	private Register createTableLiteral(Tree tree) {
		TreeTypes.expect(TABLE, tree);
		int index = 1;
		Map<Register, Register> table = new HashMap<>();
		for (Object obj : ((CommonTree) tree).getChildren()) {
			var child = TreeTypes.expect(FIELD, (CommonTree) obj);
			boolean hasKey = child.getChildCount() == 2;
			if (!hasKey && child.getChildCount() != 1) throw new AssertionError();
			if (hasKey) {
				var key = flattenExpression(child.getChild(0));
				var value = flattenExpression(child.getChild(1));
				table.put(key, value);
			} else {
				int key = index++;
				Register keyRegister = Register.create();
				steps.add(StepFactory.constNumber(keyRegister, key));
				var value = flattenExpression(child.getChild(0));
				table.put(keyRegister, value);
			}
		}
		Register result = Register.create();
		steps.add(StepFactory.createTable(table, result));
		return result;
	}

	private void createAssignment(CommonTree t, boolean local) {
		var nameList = t.getChild(0);
		TreeTypes.expect(local ? NAME_LIST : VAR_LIST, nameList);
		List<String> names = ((CommonTree) nameList).getChildren().stream()
				.map(Object::toString)
				.collect(toList());
		var valueList = TreeTypes.expect(EXPR_LIST, t.getChild(1));
		List<Register> registers = new ArrayList<>();
		for (Object o : ((CommonTree) valueList).getChildren()) {
			Tree tree = (Tree) o;
			Register register = flattenExpression(tree);
			registers.add(register);
		}
		if (local) {
			for (var name : names) {
				Step step = StepFactory.declareLocal(name);
				steps.add(step);
			}
		}
		steps.add(StepFactory.assign(names, registers));
	}

	@Nullable
	@Contract("_, true -> !null; _, false -> null")
	private Register createFunctionCall(Tree t, boolean expression) {
		Objects.requireNonNull(t);
		var function = flattenExpression(t.getChild(0));
		var call = TreeTypes.expect(CALL, t.getChild(1));
		var args = ((CommonTree) call).getChildren();
		if (args == null) args = List.of();
		List<Register> arguments = new ArrayList<>();
		for (Object tree : args) {
			Register arg = flattenExpression((Tree) tree);
			arguments.add(arg);
		}
		if (expression) {
			Register register = Register.createVararg();
			steps.add(StepFactory.call(function, arguments, register));
			return register;
		} else {
			steps.add(StepFactory.call(function, arguments));
			return null;
		}
	}

	private Register createFunctionLiteral(Tree t) {
		TreeTypes.expect(FUNCTION, t);
		Tree chunk = TreeTypes.expectChild(CHUNK, t, 1);
		List<Step> body = flatten((CommonTree) chunk);
		Tree paramList = TreeTypes.expectChild(PARAM_LIST, t, 0);
		var params = ParameterList.parse(((CommonTree) paramList));
		Register out = Register.create();
		steps.add(StepFactory.functionLiteral(body, out, params));
		return out;
	}
}
