package optic.lua.asm;

import optic.lua.asm.LValue.Name;
import optic.lua.asm.LValue.*;
import optic.lua.messages.*;
import optic.lua.optimization.TypeStatus;
import optic.lua.util.*;
import org.antlr.runtime.tree.*;
import org.jetbrains.annotations.*;
import org.slf4j.*;

import java.lang.String;
import java.util.*;

import static nl.bigo.luaparser.Lua52Walker.Name;
import static nl.bigo.luaparser.Lua52Walker.Number;
import static nl.bigo.luaparser.Lua52Walker.String;
import static nl.bigo.luaparser.Lua52Walker.*;

/**
 * Mutable implementation of tree flattener. Good startup performance.
 * Need to investigate parallelism capabilities.
 */
public class MutableFlattener {
	private static final Logger log = LoggerFactory.getLogger(MutableFlattener.class);
	private final List<Step> steps;
	// local variable info
	private final Map<String, VariableInfo> locals = new HashMap<>(8);
	// parent scope
	private final MutableFlattener parent;
	// whether or not local variables from parent are accessed as upvalues
	private final boolean lexicalBoundary;
	private final VariableInfo _ENV = new VariableInfo("_ENV");
	private final Context context;

	{
		_ENV.markAsUpvalue();
	}

	private MutableFlattener(List<Step> steps, MutableFlattener parent, boolean boundary, Context context) {
		this.parent = parent;
		lexicalBoundary = boundary;
		Objects.requireNonNull(steps);
		Objects.requireNonNull(context);
		this.context = context;
		this.steps = steps;
	}

	public Flattener getInterface() {
		return new Flattener() {
			@Override
			public AsmBlock flatten(CommonTree tree, boolean boundary, List<VariableInfo> locals) throws CompilationFailure {
				return MutableFlattener.flatten(tree, context, MutableFlattener.this, boundary, locals);
			}

			@Override
			public FlatExpr flattenExpression(CommonTree tree) throws CompilationFailure {
				List<Step> steps = new ArrayList<>();
				var flattener = new MutableFlattener(steps, MutableFlattener.this, false, context);
				Register result = flattener.flattenExpression(tree);
				return new FlatExpr(flattener.steps, result);
			}
		};
	}

	public static AsmBlock flatten(CommonTree tree, Context context) throws CompilationFailure {
		return flatten(tree, context, null, false, List.of());
	}

	private static AsmBlock flatten(CommonTree tree, Context context, MutableFlattener parent, boolean boundary, List<VariableInfo> locals) throws CompilationFailure {
		Objects.requireNonNull(tree);
		var statements = Optional.ofNullable(tree.getChildren()).orElse(List.of());
		int expectedSize = statements.size() * 4 + 10;
		var f = new MutableFlattener(new ArrayList<>(expectedSize), parent, boundary, context);
		for (var local : locals) {
			f.locals.put(local.getName(), local);
		}
		for (var stat : statements) {
			f.steps.add(StepFactory.comment("line " + ((Tree) stat).getLine()));
			try {
				f.flattenStatement((Tree) stat);
			} catch (RuntimeException e) {
				f.emit(Level.ERROR, "Unhandled exception", ((Tree) stat), e);
				throw new CompilationFailure();
			}
		}
		return new AsmBlock(f.steps, f.locals);
	}

	private AsmBlock flattenBlock(CommonTree tree) throws CompilationFailure {
		return flatten(tree, context, this, false, List.of());
	}

	private AsmBlock flattenForRangeBody(CommonTree tree, String name) throws CompilationFailure {
		var info = new VariableInfo(name);
		info.enableNumbers();
		return flatten(tree, context, this, false, List.of(info));
	}

	private AsmBlock flattenFunctionBody(CommonTree tree, ParameterList params) throws CompilationFailure {
		var infos = new ArrayList<VariableInfo>(params.list().size());
		for (var param : params.list()) {
			var info = new VariableInfo(param);
			info.enableObjects();
			info.enableNumbers();
			infos.add(info);
		}
		return flatten(tree, context, this, true, infos);
	}

	@Nullable
	private VariableInfo resolve(String name) {
		var localVar = locals.get(name);
		if (localVar != null) {
			return localVar;
		}
		if (name.equals("_ENV")) {
			return _ENV;
		}
		if (parent == null) {
			return null;
		}
		var parentVar = parent.resolve(name);
		if (parentVar != null) {
			if (lexicalBoundary) {
				parentVar.markAsUpvalue();
			}
			return parentVar;
		}
		return null;
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
				Register from = toNumber(flattenExpression(t.getChild(1)));
				Register to = toNumber(flattenExpression(t.getChild(2)));
				CommonTree block = (CommonTree) t.getChild(3).getChild(0);
				AsmBlock body = flattenForRangeBody(block, varName);
				VariableInfo counter = body.locals().get(varName);
				steps.add(StepFactory.forRange(counter, from, to, body));
				return;
			}
			case Do: {
				CommonTree block = (CommonTree) t;
				steps.add(StepFactory.doBlock(flattenBlock(block)));
				return;
			}
			case CHUNK: {
				CommonTree block = (CommonTree) t;
				steps.addAll(flattenBlock(block).steps());
				return;
			}
			case Return: {
				List<Register> registers = new ArrayList<>(t.getChildCount());
				for (Object o : Trees.childrenOf(t)) {
					Tree tree = (Tree) o;
					Register register = flattenExpression(tree);
					registers.add(register);
				}
				steps.add(StepFactory.returnFromFunction(normalizeValueList(registers)));
				return;
			}
			case If: {
				var builder = new IfElseChainBuilder(getInterface());
				for(var x : Trees.childrenOf(t)) {
					builder.add((Tree) x);
				}
				steps.add(StepFactory.ifThenChain(builder.build()));
				return;
			}
		}
		emit(Level.ERROR, "Unknown statement: " + t.toStringTree(), t);
		throw new CompilationFailure();
	}

	private Register flattenExpression(Tree t) throws CompilationFailure {
		Objects.requireNonNull(t);
		if (Operators.isBinary(t)) {
			var register = RegisterFactory.create();
			var a = discardRemaining(flattenExpression(t.getChild(0)));
			var b = discardRemaining(flattenExpression(t.getChild(1)));
			if (Operators.isMathOp(t)) {
				register.addStatusDependency(a::status);
				if(!context.options().contains(Option.FIRST_NUM_OPERATORS)) {
					register.addStatusDependency(b::status);
				}
			} else {
				register.updateStatus(TypeStatus.OBJECT);
			}
			String op = t.getText();
			steps.add(StepFactory.binaryOperator(a, b, op, register));
			return register;
		}
		if (Operators.isUnary(t)) {
			var register = RegisterFactory.create();
			String op = Operators.getUnarySymbol(t);
			Register param = discardRemaining(flattenExpression(t.getChild(0)));
			steps.add(StepFactory.unaryOperator(param, op, register));
			return register;
		}
		switch (t.getType()) {
			case Number: {
				return constant(Double.parseDouble(t.getText()));
			}
			case String: {
				var register = RegisterFactory.create();
				String value = (t.getText());
				steps.add(StepFactory.constString(register, value));
				register.updateStatus(TypeStatus.OBJECT);
				return register;
			}
			case Name: {
				var register = RegisterFactory.create();
				String name = t.getText();
				steps.add(createReadStep(name, register));
				return register;
			}
			case VAR: {
				if (t.getChild(1).getType() == CALL) {
					return createFunctionCall(t, true);
				} else {
					Trees.expectChild(INDEX, t, 1);
					var table = flattenExpression(t.getChild(0));
					var key = flattenExpression(t.getChild(1).getChild(0));
					var out = RegisterFactory.create();
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
				var reg = RegisterFactory.createVararg();
				steps.add(StepFactory.getVarargs(reg));
				return reg;
			}
			case CONDITION: {
				return flattenExpression(t.getChild(0));
			}
			case Nil: {
				return nil();
			}
			case True: {
				return constant(true);
			}
			case False: {
				return constant(false);
			}
		}
		emit(Level.ERROR, "Unknown expression: " + t + " in " + t.getParent().toStringTree(), t);
		throw new CompilationFailure();
	}

	private Register constant(boolean value) {
		var r = RegisterFactory.create();
		steps.add(StepFactory.constBool(r, value));
		return r;
	}

	private Register createTableLiteral(Tree tree) throws CompilationFailure {
		Trees.expect(TABLE, tree);
		int arrayFieldIndex = 1;
		Map<Register, Register> table = new HashMap<>();
		List<?> fields = Trees.childrenOf(tree);
		int fieldCount = fields.size();
		int fieldIndex = 0;
		for (Object obj : fields) {
			var field = Trees.expect(FIELD, (CommonTree) obj);
			boolean hasKey = field.getChildCount() == 2;
			if (!(field.getChildCount() == 1 || field.getChildCount() == 2)) {
				emit(Level.ERROR, "Expected 1 or 2 children in " + tree.toStringTree(), tree);
			}
			if (hasKey) {
				var key = flattenExpression(field.getChild(0));
				var value = flattenExpression(field.getChild(1));
				table.put(key, value);
			} else {
				var key = constant(arrayFieldIndex++);
				var value = flattenExpression(field.getChild(0));
				boolean isLastField = fieldIndex == fieldCount - 1;
				table.put(key, isLastField ? value : discardRemaining(value));
			}
			fieldIndex++;
		}
		Register result = RegisterFactory.create();
		steps.add(StepFactory.createTable(table, result));
		return result;
	}

	@Contract(mutates = "this")
	private Register toNumber(Register a) {
		var b = RegisterFactory.create();
		steps.add(StepFactory.toNumber(a, b));
		b.updateStatus(TypeStatus.NUMBER);
		return b;
	}

	@Contract(mutates = "this")
	private Register discardRemaining(Register vararg) {
		if (!vararg.isVararg()) {
			return vararg;
		}
		Register first = RegisterFactory.create();
		steps.add(StepFactory.select(first, vararg, 0));
		return first;
	}

	@Contract(mutates = "this")
	private List<Register> normalizeValueList(List<Register> registers) {
		var values = new ArrayList<Register>(registers.size());
		int valueIndex = 0;
		int valueCount = registers.size();
		for (var register : registers) {
			boolean isLastValue = valueIndex == valueCount - 1;
			if (isLastValue) {
				values.add(register);
			} else {
				values.add(discardRemaining(register));
			}
			valueIndex++;
		}
		return values;
	}

	@Contract(mutates = "this")
	private List<Register> flattenAll(List<?> trees) throws CompilationFailure {
		int size = trees.size();
		var registers = new ArrayList<Register>(size);
		for (Object tree : trees) {
			registers.add(flattenExpression((Tree) tree));
		}
		return registers;
	}

	@Contract(mutates = "this")
	private LValue createLValue(Object name) throws CompilationFailure {
		if (name instanceof Tree && ((Tree) name).getType() == ASSIGNMENT_VAR) {
			// table assignment
			var assignment = ((CommonTree) name);
			var table = flattenExpression(assignment.getChild(0));
			Trees.expectChild(INDEX, assignment, 1);
			var key = flattenExpression(assignment.getChild(1).getChild(0));
			return new LValue.TableField(table, key);
		} else {
			// variable assignment;
			return new LValue.Name(name.toString());
		}
	}

	@Contract(mutates = "this")
	private Register nil() {
		var nil = RegisterFactory.create();
		steps.add(StepFactory.constNil(nil));
		nil.updateStatus(TypeStatus.OBJECT);
		return nil;
	}

	@Contract(mutates = "this")
	private Register constant(double d) {
		var num = RegisterFactory.create();
		steps.add(StepFactory.constNumber(num, d));
		num.updateStatus(TypeStatus.NUMBER);
		return num;
	}

	@Contract(mutates = "this")
	private Step createWriteStep(LValue left, Register value) {
		if (left instanceof LValue.TableField) {
			return StepFactory.tableWrite((TableField) left, value);
		} else if (left instanceof LValue.Name) {
			var name = ((Name) left);
			VariableInfo info = resolve(name.name());
			if (info == null) {
				return StepFactory.write(VariableInfo.global(name.name()), value);
			}
			info.update(value.status());
			info.markAsWritten();
			return StepFactory.write(info, value);
		}
		throw new AssertionError();
	}

	@Contract(mutates = "this")
	private Step createReadStep(String name, Register out) {
		VariableInfo info = resolve(name);
		if (info == null) {
			var global = VariableInfo.global(name);
			return StepFactory.read(global, out);
		}
		out.addStatusDependency(info::status);
		return StepFactory.read(info, out);
	}

	@Contract(mutates = "this")
	private void createAssignment(CommonTree t, boolean local) throws CompilationFailure {
		var nameList = t.getChild(0);
		Trees.expect(local ? NAME_LIST : VAR_LIST, nameList);
		var valueList = Trees.expect(EXPR_LIST, t.getChild(1));
		List<Register> values = normalizeValueList(flattenAll(Trees.childrenOf(valueList)));
		List<?> names = Trees.childrenOf(nameList);
		if (local) {
			// if the assignment starts with local, no need to worry about table assignments
			// this code is illegal: "local a, tb[k] = 4, 2"
			for (var name : names) {
				locals.put(name.toString(), new VariableInfo(name.toString()));
				Step step = StepFactory.declareLocal(resolve(name.toString()));
				steps.add(step);
			}
		}
		List<LValue> variables = new ArrayList<>(names.size());
		for (var name : names) {
			variables.add(createLValue(name));
		}
		boolean endsWithVararg = !values.isEmpty() && values.get(values.size() - 1).isVararg();
		Register vararg = endsWithVararg ? values.get(values.size() - 1) : null;
		if (endsWithVararg) {
			Objects.requireNonNull(vararg);
		}
		int nonVarargRegisters = values.size() - (endsWithVararg ? 1 : 0);
		int overflow = 0;
		for (int i = 0; i < variables.size(); i++) {
			LValue variable = variables.get(i);
			if (i < nonVarargRegisters) {
				Register value = values.get(i);
				steps.add(createWriteStep(variable, value));
			} else if (vararg == null) {
				steps.add(createWriteStep(variable, nil()));
			} else {
				Register selected = RegisterFactory.create();
				steps.add(StepFactory.select(selected, vararg, overflow));
				steps.add(createWriteStep(variable, selected));
				overflow++;
			}
		}
	}

	@Nullable
	@Contract(value = "_, true -> !null; _, false -> null", mutates = "this")
	private Register createFunctionCall(Tree t, boolean expression) throws CompilationFailure {
		Objects.requireNonNull(t);
		var function = flattenExpression(t.getChild(0));
		var call = Trees.expect(CALL, t.getChild(1));
		List<Register> arguments = normalizeValueList(flattenAll(Trees.childrenOf(call)));
		if (expression) {
			Register register = RegisterFactory.createVararg();
			steps.add(StepFactory.call(function, arguments, register));
			return register;
		} else {
			steps.add(StepFactory.call(function, arguments));
			return null;
		}
	}

	@Contract(mutates = "this")
	private Register createFunctionLiteral(Tree t) throws CompilationFailure {
		Trees.expect(FUNCTION, t);
		Tree paramList = Trees.expectChild(PARAM_LIST, t, 0);
		var params = ParameterList.parse(((CommonTree) paramList));
		Tree chunk = Trees.expectChild(CHUNK, t, 1);
		AsmBlock body = flattenFunctionBody((CommonTree) chunk, params);
		Register out = RegisterFactory.create();
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
		context.reporter().report(warning);
	}
}
