package optic.lua.ssa;

import optic.lua.messages.*;
import optic.lua.util.*;
import org.antlr.runtime.tree.*;
import org.jetbrains.annotations.*;
import org.slf4j.*;

import java.lang.String;
import java.util.*;

import static java.lang.Double.parseDouble;
import static nl.bigo.luaparser.Lua52Walker.Number;
import static nl.bigo.luaparser.Lua52Walker.String;
import static nl.bigo.luaparser.Lua52Walker.*;

/**
 * Mutable implementation of SSA translator. Good startup performance.
 * Need to investigate parallelism capabilities.
 */
public class MutableFlattener {
	private static final Logger log = LoggerFactory.getLogger(MutableFlattener.class);
	private final List<Step> steps;
	private final MessageReporter reporter;

	private MutableFlattener(List<Step> steps, MessageReporter reporter) {
		Objects.requireNonNull(steps);
		Objects.requireNonNull(reporter);
		this.reporter = reporter;
		this.steps = steps;
	}

	public static List<Step> flatten(CommonTree tree, MessageReporter reporter) throws CompilationFailure {
		Objects.requireNonNull(tree);
		var statements = Optional.ofNullable(tree.getChildren()).orElse(List.of());
		int expectedSize = statements.size() * 4 + 10;
		var f = new MutableFlattener(new ArrayList<>(expectedSize), reporter);
		for (var stat : statements) {
			f.steps.add(StepFactory.comment("line " + ((Tree) stat).getLine()));
			try {
				f.flattenStatement((Tree) stat);
			} catch (RuntimeException e) {
				f.emit(Level.ERROR, "Unhandled exception", ((Tree) stat), e);
				throw new CompilationFailure();
			}
		}
		return f.steps;
	}

	private List<Step> flattenChunk(CommonTree tree) throws CompilationFailure {
		return flatten(tree, reporter);
	}

	private void flattenStatement(Tree t) throws CompilationFailure {
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
				if (t.getChild(1).getType() == CALL) {
					createFunctionCall(t, false);
				} else {
					var msg = "Not implemented yet: " + t.toStringTree();
					steps.add(StepFactory.comment(msg));
					emit(Level.WARNING, msg, t);
				}
				return;
			}
			case For: {
				String varName = t.getChild(0).toString();
				Register from = flattenExpression(t.getChild(1));
				Register to = flattenExpression(t.getChild(2));
				CommonTree block = (CommonTree) t.getChild(3).getChild(0);
				List<Step> body = flattenChunk(block);
				steps.add(StepFactory.forRange(varName, from, to, body));
				return;
			}
			case Do: {
				CommonTree block = (CommonTree) t;
				steps.add(StepFactory.doBlock(flattenChunk(block)));
				return;
			}
			case CHUNK: {
				CommonTree block = (CommonTree) t;
				steps.addAll(flattenChunk(block));
				return;
			}
			case Return: {
				List<Register> registers = new ArrayList<>(t.getChildCount());
				for (Object o : Trees.childrenOf(t)) {
					Tree tree = (Tree) o;
					Register register = flattenExpression(tree);
					registers.add(register);
				}
				steps.add(StepFactory.returnFromFunction(registers));
				return;
			}
			case If: {
				Register condition = flattenExpression(t.getChild(0).getChild(0));
				List<Step> then = flattenChunk((CommonTree) t.getChild(0).getChild(1));
				steps.add(StepFactory.ifThen(condition, then));
				if (t.getChildCount() > 1) {
					var children = Trees.childrenOf(t);
					var remaining = children.subList(1, children.size());
					var msg = "Not implemented yet: " + remaining;
					steps.add(StepFactory.comment(msg));
					emit(Level.WARNING, msg, t);
					return;
				}
				return;
			}
		}
		emit(Level.ERROR, "Unknown statement: " + t.toStringTree(), t);
		throw new CompilationFailure();
	}

	private Register flattenExpression(Tree t) throws CompilationFailure {
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
				if (t.getChild(1).getType() == CALL) {
					return createFunctionCall(t, true);
				} else {
					Trees.expectChild(INDEX, t, 1);
					var table = flattenExpression(t.getChild(0));
					var key = flattenExpression(t.getChild(1).getChild(0));
					var out = Register.create();
					steps.add(StepFactory.tableIndex(table, key, out));
					return out;
				}
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
			case CONDITION: {
				return flattenExpression(t.getChild(0));
			}
			case Nil: {
				Register nil = Register.create();
				steps.add(StepFactory.constNil(nil));
				return nil;
			}
		}
		emit(Level.ERROR, "Unknown expression: " + t, t);
		throw new CompilationFailure();
	}

	private Register createTableLiteral(Tree tree) throws CompilationFailure {
		Trees.expect(TABLE, tree);
		int index = 1;
		Map<Register, Register> table = new HashMap<>();
		for (Object obj : Trees.childrenOf(tree)) {
			var child = Trees.expect(FIELD, (CommonTree) obj);
			boolean hasKey = child.getChildCount() == 2;
			if (!hasKey && child.getChildCount() != 1) {
				emit(Level.ERROR, "Expected 1 or 2 children in " + tree.toStringTree(), tree);
			}
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

	private void createAssignment(CommonTree t, boolean local) throws CompilationFailure {
		var nameList = t.getChild(0);
		Trees.expect(local ? NAME_LIST : VAR_LIST, nameList);
		var valueList = Trees.expect(EXPR_LIST, t.getChild(1));
		List<Register> registers = new ArrayList<>();
		for (Object o : Trees.childrenOf(valueList)) {
			Tree tree = (Tree) o;
			Register register = flattenExpression(tree);
			registers.add(register);
		}
		List<?> names = Trees.childrenOf(nameList);
		if (local) {
			// if the assignment starts with local, no need to worry about table assignments
			// this code is illegal: "local a, tb[k] = 4, 2"
			for (var name : names) {
				Step step = StepFactory.declareLocal(name.toString());
				steps.add(step);
			}
		}
		List<LValue> targets = new ArrayList<>(names.size());
		for (var name : names) {
			if (name instanceof Tree && ((Tree) name).getType() == ASSIGNMENT_VAR) {
				// table assignment
				var assignment = ((CommonTree) name);
				var table = flattenExpression(assignment.getChild(0));
				Trees.expectChild(INDEX, assignment, 1);
				var key = flattenExpression(assignment.getChild(1).getChild(0));
				targets.add(LValue.tableKey(table, key));
			} else {
				// variable assignment;
				targets.add(LValue.variable(name.toString()));
			}
		}
		steps.add(StepFactory.assign(targets, registers));
	}

	@Nullable
	@Contract("_, true -> !null; _, false -> null")
	private Register createFunctionCall(Tree t, boolean expression) throws CompilationFailure {
		Objects.requireNonNull(t);
		var function = flattenExpression(t.getChild(0));
		var call = Trees.expect(CALL, t.getChild(1));
		var args = Trees.childrenOf(call);
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

	private Register createFunctionLiteral(Tree t) throws CompilationFailure {
		Trees.expect(FUNCTION, t);
		Tree chunk = Trees.expectChild(CHUNK, t, 1);
		List<Step> body = flattenChunk((CommonTree) chunk);
		Tree paramList = Trees.expectChild(PARAM_LIST, t, 0);
		var params = ParameterList.parse(((CommonTree) paramList));
		Register out = Register.create();
		steps.add(StepFactory.functionLiteral(body, out, params));
		return out;
	}

	private void emit(Level level, String msg, Tree location) {
		emit(level, msg, location, null);
	}

	private void emit(Level level, String msg, Tree location, Throwable cause) {
		var warning = Message.create(msg);
		warning.setLine(location.getLine());
		warning.setColumn(location.getCharPositionInLine());
		warning.setLevel(level);
		warning.setCause(cause);
		reporter.report(warning);
	}
}
