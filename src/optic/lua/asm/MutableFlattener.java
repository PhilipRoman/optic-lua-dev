package optic.lua.asm;

import optic.lua.asm.instructions.VariableMode;
import optic.lua.messages.*;
import optic.lua.optimization.*;
import optic.lua.util.*;
import org.antlr.runtime.tree.*;
import org.jetbrains.annotations.*;
import org.slf4j.*;

import java.lang.String;
import java.util.*;

import static nl.bigo.luaparser.Lua52Walker.Number;
import static nl.bigo.luaparser.Lua52Walker.String;
import static nl.bigo.luaparser.Lua52Walker.*;

/**
 * Mutable implementation of tree flattener. Good startup performance.
 * Need to investigate parallelism capabilities.
 */
public class MutableFlattener implements VariableResolver {
	private static final Logger log = LoggerFactory.getLogger(MutableFlattener.class);
	private final List<Step> steps;
	// local variable info
	private final Map<String, VariableInfo> locals = new HashMap<>(8);
	// parent scope
	private final MutableFlattener parent;
	// whether or not local variables from parent are accessed as upvalues
	private final boolean lexicalBoundary;
	private final VariableInfo _ENV = VariableInfo.createEnv();
	private final Context context;
	private final BlockMeaning meaning;

	private MutableFlattener(List<Step> steps, MutableFlattener parent, boolean boundary, Context context, BlockMeaning meaning) {
		this.parent = parent;
		lexicalBoundary = boundary;
		this.meaning = meaning;
		Objects.requireNonNull(steps);
		Objects.requireNonNull(context);
		this.context = context;
		this.steps = steps;
	}

	public Flattener getInterface() {
		return new Flattener() {
			@Override
			public AsmBlock flatten(CommonTree tree, List<VariableInfo> locals, BlockMeaning meaning) throws CompilationFailure {
				return MutableFlattener.flatten(tree, context, MutableFlattener.this, locals, meaning);
			}

			@Override
			public FlatExpr flattenExpression(CommonTree tree) throws CompilationFailure {
				List<Step> steps = new ArrayList<>();
				var flattener = new MutableFlattener(steps, MutableFlattener.this, false, context, meaning);
				RValue result = flattener.flattenExpression(tree);
				return new FlatExpr(flattener.steps, result);
			}
		};
	}

	public static AsmBlock flatten(CommonTree tree, Context context) throws CompilationFailure {
		return flatten(tree, context, null, List.of(), BlockMeaning.MAIN_CHUNK);
	}

	private static AsmBlock flatten(CommonTree tree, Context context, MutableFlattener parent, List<VariableInfo> locals, BlockMeaning kind) throws CompilationFailure {
		Objects.requireNonNull(tree);
		var statements = Optional.ofNullable(tree.getChildren()).orElse(List.of());
		int expectedSize = statements.size() * 4 + 10;
		var f = new MutableFlattener(new ArrayList<>(expectedSize), parent, kind.hasLexicalBoundary(), context, kind);
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

	private AsmBlock flattenDoBlock(CommonTree tree) throws CompilationFailure {
		return flatten(tree, context, this, List.of(), BlockMeaning.DO_BLOCK);
	}

	private AsmBlock flattenForRangeBody(CommonTree tree, ProvenType counterType, String name) throws CompilationFailure {
		var info = new VariableInfo(name);
		info.update(counterType);
		return flatten(tree, context, this, List.of(info), BlockMeaning.LOOP_BODY);
	}

	private AsmBlock flattenFunctionBody(CommonTree tree, ParameterList params) throws CompilationFailure {
		var infos = new ArrayList<VariableInfo>(params.list().size());
		for (var param : params.list()) {
			var info = new VariableInfo(param);
			info.enableObjects();
			info.enableNumbers();
			infos.add(info);
		}
		return flatten(tree, context, this, infos, BlockMeaning.FUNCTION_BODY);
	}

	@Nullable
	@Override
	public VariableInfo resolve(String name) {
		var localVar = locals.get(name);
		if (localVar != null) {
			return localVar;
		}
		if (name.equals("_ENV") && meaning == BlockMeaning.MAIN_CHUNK) {
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
				var builder = new NestedFieldBuilder(getInterface(), flattenExpression(t.getChild(0)));
				int i = 1;
				while (i < t.getChildCount() && t.getChild(i).getType() == INDEX) {
					builder.add(t.getChild(i++));
				}
				if (t.getChild(i) != null && t.getChild(i).getType() == CALL) {
					var result = builder.build();
					steps.addAll(result.block());
					createFunctionCall(result.value(), t.getChild(i), false);
				} else {
					var result = builder.build();
					steps.addAll(result.block());
				}
				return;
			}
			case For: {
				String varName = t.getChild(0).toString();
				RValue from = toNumber(flattenExpression(t.getChild(1)));
				RValue to = toNumber(flattenExpression(t.getChild(2)));
				CommonTree block = (CommonTree) t.getChild(3).getChild(0);
				AsmBlock body = flattenForRangeBody(block, from.typeInfo(), varName);
				VariableInfo counter = body.locals().get(varName);
				steps.add(StepFactory.forRange(counter, from, to, body));
				return;
			}
			case Do: {
				CommonTree block = (CommonTree) t;
				steps.add(StepFactory.doBlock(flattenDoBlock(block)));
				return;
			}
			case CHUNK: {
				CommonTree block = (CommonTree) t;
				steps.addAll(flattenDoBlock(block).steps());
				return;
			}
			case Return: {
				List<RValue> values = new ArrayList<>(t.getChildCount());
				for (Object o : Trees.childrenOf(t)) {
					Tree tree = (Tree) o;
					RValue value = flattenExpression(tree);
					values.add(value);
				}
				steps.add(StepFactory.returnFromFunction(normalizeValueList(values)));
				return;
			}
			case If: {
				var builder = new IfElseChainBuilder(getInterface());
				for (var x : Trees.childrenOf(t)) {
					builder.add((Tree) x);
				}
				steps.add(StepFactory.ifThenChain(builder.build()));
				return;
			}
		}
		emit(Level.ERROR, "Unknown statement: " + t.toStringTree(), t);
		throw new CompilationFailure();
	}

	private RValue flattenExpression(Tree t) throws CompilationFailure {
		Objects.requireNonNull(t);
		if (Operators.isBinary(t)) {
			var register = RegisterFactory.create();
			// Lua does not have methods for >= and >
			// instead we reverse the arguments and use < and <= respectively
			boolean reverse = t.getType() == GT || t.getType() == GTEq;
			LuaOperator op = LuaOperator.forTokenType(t.getType());
			var a = discardRemaining(flattenExpression(t.getChild(reverse ? 1 : 0)));
			var b = discardRemaining(flattenExpression(t.getChild(reverse ? 0 : 1)));
			register.addTypeDependency(() -> op.resultType(a.typeInfo(), b.typeInfo()));
			steps.add(StepFactory.binaryOperator(a, b, op, register));
			return register;
		}
		if (Operators.isUnary(t)) {
			var register = RegisterFactory.create();
			LuaOperator op = LuaOperator.forTokenType(t.getType());
			RValue param = discardRemaining(flattenExpression(t.getChild(0)));
			register.addTypeDependency(() -> op.resultType(null, param.typeInfo()));
			steps.add(StepFactory.unaryOperator(param, op, register));
			return register;
		}
		switch (t.getType()) {
			case Number: {
				return RValue.number(Double.parseDouble(t.getText()));
			}
			case String: {
				return RValue.string(t.getText());
			}
			case Name: {
				var register = RegisterFactory.create();
				String name = t.getText();
				steps.add(createReadStep(name, register));
				return register;
			}
			case VAR: {
				var builder = new NestedFieldBuilder(getInterface(), flattenExpression(t.getChild(0)));
				int i = 1;
				while (i < t.getChildCount() && t.getChild(i).getType() == INDEX) {
					builder.add(t.getChild(i++));
				}
				if (t.getChild(i) != null && t.getChild(i).getType() == CALL) {
					var result = builder.build();
					steps.addAll(result.block());
					return createFunctionCall(result.value(), t.getChild(i), true);
				} else {
					var result = builder.build();
					steps.addAll(result.block());
					return result.value();
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
				return RValue.nil();
			}
			case True: {
				return RValue.bool(true);
			}
			case False: {
				return RValue.bool(false);
			}
		}
		emit(Level.ERROR, "Unknown expression: " + t + " in " + t.getParent().toStringTree(), t);
		throw new CompilationFailure();
	}

	private Register createTableLiteral(Tree tree) throws CompilationFailure {
		Trees.expect(TABLE, tree);
		List<?> fields = Trees.childrenOf(tree);
		var builder = new TableLiteralBuilder(this.getInterface(), fields.size());
		for (Object obj : fields) {
			builder.addEntry((Tree) obj);
		}
		Register result = RegisterFactory.create();
		steps.addAll(builder.getSteps());
		steps.add(StepFactory.assign(result, RValue.table(builder.getTable())));
		return result;
	}

	@Contract(mutates = "this")
	private RValue toNumber(RValue a) {
		a = discardRemaining(a);
		if (a.typeInfo().isNumeric()) {
			return a;
		}
		var b = RegisterFactory.create();
		steps.add(StepFactory.toNumber(a, b));
		b.updateStatus(ProvenType.NUMBER);
		return b;
	}

	@Contract(mutates = "this")
	private RValue discardRemaining(RValue vararg) {
		if(vararg instanceof Register) {
			return ((Register) vararg).discardRemaining().applyTo(steps);
		}
		return vararg;
	}

	@Contract(mutates = "this")
	private List<RValue> normalizeValueList(List<RValue> registers) {
		var values = new ArrayList<RValue>(registers.size());
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
	private List<RValue> flattenAll(List<?> trees) throws CompilationFailure {
		int size = trees.size();
		var registers = new ArrayList<RValue>(size);
		for (Object tree : trees) {
			registers.add(flattenExpression((Tree) tree));
		}
		return registers;
	}

	@Contract(mutates = "this")
	private LValue createLValue(Object name) throws CompilationFailure {
		if (name instanceof Tree && ((Tree) name).getType() == ASSIGNMENT_VAR) {
			// table assignment
			var t = (CommonTree) name;
			var builder = new NestedFieldBuilder(getInterface(), flattenExpression(t.getChild(0)));
			int i = 1;
			while (i < t.getChildCount() - 1 && t.getChild(i).getType() == INDEX) {
				builder.add(t.getChild(i++));
			}
			var result = builder.build();
			steps.addAll(result.block());
			Trees.expect(INDEX, t.getChild(t.getChildCount() - 1));
			var key = flattenExpression(t.getChild(t.getChildCount() - 1).getChild(0));
			return new LValue.TableField(result.value(), key);
		} else {
			// variable assignment;
			return new LValue.Name(name.toString());
		}
	}

	private Step createReadStep(String name, Register out) {
		VariableInfo info = resolve(name);
		if (info == null) {
			var global = VariableInfo.global(name);
			out.updateStatus(ProvenType.OBJECT);
			return StepFactory.read(global, out);
		}
		out.addTypeDependency(info::status);
		return StepFactory.read(info, out);
	}

	@Contract(mutates = "this")
	private void createAssignment(CommonTree t, boolean local) throws CompilationFailure {
		var nameList = t.getChild(0);
		Trees.expect(local ? NAME_LIST : VAR_LIST, nameList);

		var builder = new AssignmentBuilder(this);
		var valueList = Trees.expect(EXPR_LIST, t.getChild(1));
		List<RValue> values = normalizeValueList(flattenAll(Trees.childrenOf(valueList)));
		builder.addValues(values);
		List<?> names = Trees.childrenOf(nameList);

		// if the assignment starts with local, no need to worry about table assignments
		// this code is illegal: "local a, tb[k] = 4, 2"
		for (var name : names) {
			var info = resolve(name.toString());
			if (info == null || info.getMode() != VariableMode.LOCAL) {
				// if variable doesn't exist yet
				if (local) {
					locals.put(name.toString(), new VariableInfo(name.toString()));
					declare(name.toString());
				}
			} else if (!meaning.isConditional() && context.options().get(StandardFlags.SSA_SPLIT)) {
				// if variable is already a local variable
				var next = info.nextIncarnation();
				locals.put(name.toString(), next);
				declare(next.getName());
			}
		}

		for (var name : names) {
			builder.addVariable(createLValue(name));
		}
		steps.addAll(builder.build());
	}

	@Contract(mutates = "this")
	private void declare(String name) {
		locals.put(name, new VariableInfo(name));
		Step step = StepFactory.declareLocal(resolve(name));
		steps.add(step);
	}

	@Nullable
	@Contract(value = "_, _, true -> !null; _, _, false -> null", mutates = "this")
	private Register createFunctionCall(RValue function, Tree call, boolean expression) throws CompilationFailure {
		Objects.requireNonNull(function);
		Objects.requireNonNull(call);
		Trees.expect(CALL, call);
		List<RValue> arguments = normalizeValueList(flattenAll(Trees.childrenOf(call)));
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

	@Contract(mutates = "this")
	private RValue applyRecipe(FlatExpr expr) {
		steps.addAll(expr.block());
		return expr.value();
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
