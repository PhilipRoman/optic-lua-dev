package optic.lua.asm;

import optic.lua.messages.*;
import optic.lua.optimization.*;
import optic.lua.util.*;
import org.antlr.runtime.tree.*;
import org.jetbrains.annotations.*;
import org.slf4j.*;

import java.lang.String;
import java.util.*;

import static nl.bigo.luaparser.Lua53Walker.Number;
import static nl.bigo.luaparser.Lua53Walker.String;
import static nl.bigo.luaparser.Lua53Walker.*;

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
	private final Options options;
	private final BlockMeaning meaning;

	private MutableFlattener(List<Step> steps, MutableFlattener parent, boolean boundary, Options options, BlockMeaning meaning) {
		this.parent = parent;
		lexicalBoundary = boundary;
		this.meaning = meaning;
		Objects.requireNonNull(steps);
		Objects.requireNonNull(options);
		this.options = options;
		this.steps = steps;
	}

	public static AsmBlock flatten(CommonTree tree, Options context) throws CompilationFailure {
		return flatten(tree, context, null, List.of(), BlockMeaning.MAIN_CHUNK);
	}

	private static AsmBlock flatten(CommonTree tree, Options context, MutableFlattener parent, List<VariableInfo> locals, BlockMeaning kind) throws CompilationFailure {
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
				log.error(((Tree) stat).toStringTree(), e);
				throw new CompilationFailure();
			}
		}
		return new AsmBlock(f.steps, f.locals);
	}

	private Flattener getInterface() {
		return new Flattener() {
			@Override
			public AsmBlock flatten(CommonTree tree, List<VariableInfo> locals, BlockMeaning meaning) throws CompilationFailure {
				return MutableFlattener.flatten(tree, options, MutableFlattener.this, locals, meaning);
			}

			@Override
			public FlatExpr flattenExpression(CommonTree tree) throws CompilationFailure {
				List<Step> steps = new ArrayList<>();
				var flattener = new MutableFlattener(steps, MutableFlattener.this, false, options, meaning);
				RValue result = flattener.flattenExpression(tree);
				return new FlatExpr(flattener.steps, result);
			}
		};
	}

	private AsmBlock flattenDoBlock(CommonTree tree) throws CompilationFailure {
		return flatten(tree, options, this, List.of(), BlockMeaning.DO_BLOCK);
	}

	private AsmBlock flattenLoopBody(CommonTree tree) throws CompilationFailure {
		return flatten(tree, options, this, List.of(), BlockMeaning.LOOP_BODY);
	}

	private AsmBlock flattenForInLoopBody(CommonTree tree, List<VariableInfo> variables) throws CompilationFailure {
		return flatten(tree, options, this, variables, BlockMeaning.LOOP_BODY);
	}

	private AsmBlock flattenForRangeBody(CommonTree tree, ProvenType counterType, String name) throws CompilationFailure {
		var info = new VariableInfo(name);
		info.update(counterType);
		return flatten(tree, options, this, List.of(info), BlockMeaning.LOOP_BODY);
	}

	private AsmBlock flattenFunctionBody(CommonTree tree, ParameterList params) throws CompilationFailure {
		var infos = new ArrayList<VariableInfo>(params.list().size());
		for (var param : params.list()) {
			var info = new VariableInfo(param);
			info.enableObjects();
			info.enableNumbers();
			infos.add(info);
		}
		return flatten(tree, options, this, infos, BlockMeaning.FUNCTION_BODY);
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
				var builder = new ChainedAccessBuilder(getInterface(), flattenExpression(t.getChild(0)));
				int i = 1;
				while (i < t.getChildCount()) {
					builder.add(t.getChild(i++));
				}
				var result = builder.buildStatement();
				steps.addAll(result);
				return;
			}
			case For: {
				String varName = t.getChild(0).toString();
				RValue from = evaluateOnce(toNumber(flattenExpression(t.getChild(1))));
				RValue to = evaluateOnce(toNumber(flattenExpression(t.getChild(2))));
				Tree stepOrBody = t.getChild(3);
				if (stepOrBody.getType() == Do) {
					// for loop without step:
					// for i = A, B do ... end
					CommonTree block = (CommonTree) stepOrBody.getChild(0);
					AsmBlock body = flattenForRangeBody(block, from.typeInfo(), varName);
					VariableInfo counter = body.locals().get(varName);
					steps.add(StepFactory.forRange(counter, from, to, body));
				} else {
					// for loop with step "C":
					// for i = A, B, C do ... end
					RValue step = evaluateOnce(toNumber(flattenExpression(stepOrBody)));
					CommonTree block = (CommonTree) t.getChild(4).getChild(0);
					AsmBlock body = flattenForRangeBody(block, from.typeInfo().and(step.typeInfo()), varName);
					VariableInfo counter = body.locals().get(varName);
					steps.add(StepFactory.forRange(counter, from, to, step, body));
				}
				return;
			}
			case FOR_IN: {
				// FOR_IN (NAME_LIST <nameList>) (EXPR_LIST <iterator>) (do (CHUNK <body>))
				var nameList = (CommonTree) Trees.expectChild(NAME_LIST, t, 0);
				var variables = new ArrayList<VariableInfo>(3);
				for (var name : nameList.getChildren()) {
					variables.add(new VariableInfo(name.toString()));
				}
				if (t.getChild(1).getChildCount() > 1) {
					log.warn("for loop iterator should be a single expression; additional expressions will be ignored! {}", t.toStringTree());
				}
				var iterator = evaluateOnce(discardRemaining(flattenExpression(Trees.expectChild(EXPR_LIST, t, 1).getChild(0))));
				var body = flattenForInLoopBody((CommonTree) Trees.expectChild(Do, t, 2), variables);
				steps.add(StepFactory.forInLoop(variables, iterator, body));
				return;
			}
			case While: {
				// while (<condition>) (do (chunk (<body>)))
				var condition = getInterface().flattenExpression(t.getChild(0));
				var chunk = Trees.expect(CHUNK, t.getChild(1).getChild(0));
				var body = flattenLoopBody((CommonTree) chunk);
				var stepList = new ArrayList<Step>(body.steps().size() + 8);
				stepList.addAll(condition.block());
				stepList.add(StepFactory.breakIf(condition.value(), false));
				stepList.addAll(body.steps());
				var processedBody = new AsmBlock(stepList, body.locals());
				steps.add(StepFactory.loop(processedBody));
				return;
			}
			case Repeat: {
				// repeat (chunk (<body>)) (<condition>)
				var condition = getInterface().flattenExpression(t.getChild(1));
				var chunk = Trees.expect(CHUNK, t.getChild(0));
				var body = flattenLoopBody((CommonTree) chunk);
				var stepList = new ArrayList<>(body.steps());
				stepList.addAll(condition.block());
				stepList.add(StepFactory.breakIf(condition.value(), true));
				var processedBody = new AsmBlock(stepList, body.locals());
				steps.add(StepFactory.loop(processedBody));
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
		log.error("Unknown statement: (type: {}) at line {}: {}", Trees.reverseLookupName(t.getType()), t.getLine(), t.toStringTree());
		throw new CompilationFailure();
	}

	private RValue flattenExpression(Tree t) throws CompilationFailure {
		Objects.requireNonNull(t);
		if (Operators.isBinary(t)) {
			LuaOperator op = LuaOperator.forTokenType(t.getType());
			var a = discardRemaining(flattenExpression(t.getChild(0)));
			var b = discardRemaining(flattenExpression(t.getChild(1)));
			var register = RegisterFactory.create(() -> op.resultType(a.typeInfo(), b.typeInfo()));
			steps.add(StepFactory.assign(register, RValue.invocation(a, op.invocationMethod(), List.of(b))));
			return register;
		}
		if (Operators.isUnary(t)) {
			LuaOperator op = LuaOperator.forTokenType(t.getType());
			RValue param = discardRemaining(flattenExpression(t.getChild(0)));
			var register = RegisterFactory.create(() -> op.resultType(null, param.typeInfo()));
			steps.add(StepFactory.assign(register, RValue.invocation(param, op.invocationMethod(), List.of())));
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
				String name = t.getText();
				VariableInfo info = resolve(name);
				if (info == null) {
					var global = VariableInfo.global(name);
					return RValue.variableName(global);
				}
				return RValue.variableName(info);
			}
			case VAR: {
				var builder = new ChainedAccessBuilder(getInterface(), flattenExpression(t.getChild(0)));
				int i = 1;
				while (i < t.getChildCount()) {
					builder.add(t.getChild(i++));
				}
				var result = builder.buildExpression();
				steps.addAll(result.block());
				return result.value();
			}
			case FUNCTION: {
				return createFunctionLiteral(t);
			}
			case TABLE: {
				return createTableLiteral(t);
			}
			case DotDotDot: {
				return RValue.varargs();
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
		log.error("Unknown expression: (type: {}) at line {}: {}", Trees.reverseLookupName(t.getType()), t.getLine(), t.toStringTree());
		throw new CompilationFailure();
	}

	@Contract(mutates = "this")
	private RValue createTableLiteral(Tree tree) throws CompilationFailure {
		Trees.expect(TABLE, tree);
		List<?> fields = Trees.childrenOf(tree);
		var builder = new TableLiteralBuilder(this.getInterface(), fields.size());
		for (Object obj : fields) {
			builder.addEntry((Tree) obj);
		}
		steps.addAll(builder.getSteps());
		return RValue.table(builder.getTable());
	}

	@Contract(mutates = "this")
	private RValue toNumber(RValue a) {
		a = discardRemaining(a);
		if (a.typeInfo().isNumeric()) {
			return a;
		}
		return RValue.invocation(a, InvocationMethod.TO_NUMBER, List.of());
	}

	private RValue evaluateOnce(RValue value) {
		if (value.isPure()) {
			return value;
		}
		var register = RegisterFactory.create(value::typeInfo);
		steps.add(StepFactory.assign(register, value));
		return register;
	}

	@Contract(mutates = "this")
	private RValue discardRemaining(RValue vararg) {
		if (vararg.isVararg()) {
			Register r = RegisterFactory.create(ProvenType.OBJECT);
			steps.add(StepFactory.select(r, vararg, 0));
			return r;
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
			log.info("{}", t.toStringTree());
			var builder = new ChainedAccessBuilder(getInterface(), flattenExpression(t.getChild(0)));
			int i = 1;
			while (i < t.getChildCount()) {
				builder.add(t.getChild(i++));
			}
			steps.addAll(builder.buildExpression().block());
			return new LValue.TableField(builder.getSelf(), builder.getLastIndexKey());
		} else {
			// variable assignment;
			return new LValue.Name(name.toString());
		}
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
					var variable = new VariableInfo(name.toString());
					locals.put(name.toString(), variable);
					declare(variable);
				}
			} else if (locals.containsKey(info.getName()) && options.get(StandardFlags.SSA_SPLIT)) {
				// if variable is already a local variable
				var next = info.nextIncarnation();
				locals.put(name.toString(), next);
				declare(next);
			}
		}

		for (var name : names) {
			builder.addVariable(createLValue(name));
		}
		steps.addAll(builder.build());
	}

	@Contract(mutates = "this")
	private void declare(VariableInfo variable) {
		Step step = StepFactory.declareLocal(variable);
		steps.add(step);
	}

	@Nullable
	@Contract(value = "_, _, true -> !null; _, _, false -> null", mutates = "this")
	private RValue createFunctionCall(RValue function, Tree call, boolean expression) throws CompilationFailure {
		Objects.requireNonNull(function);
		Objects.requireNonNull(call);
		Trees.expect(CALL, call);
		List<RValue> arguments = normalizeValueList(flattenAll(Trees.childrenOf(call)));
		if (expression) {
			return RValue.invocation(function, InvocationMethod.CALL, arguments);
		} else {
			steps.add(StepFactory.discard(RValue.invocation(function, InvocationMethod.CALL, arguments)));
			return null;
		}
	}

	@Contract(pure = true)
	private RValue createFunctionLiteral(Tree t) throws CompilationFailure {
		Trees.expect(FUNCTION, t);
		Tree paramList = Trees.expectChild(PARAM_LIST, t, 0);
		var params = ParameterList.parse(((CommonTree) paramList));
		Tree chunk = Trees.expectChild(CHUNK, t, 1);
		AsmBlock body = flattenFunctionBody((CommonTree) chunk, params);
		return RValue.function(params, body);
	}
}
