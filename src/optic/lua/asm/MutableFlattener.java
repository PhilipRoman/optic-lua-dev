package optic.lua.asm;

import optic.lua.asm.ListNode.ExprList;
import optic.lua.messages.*;
import optic.lua.optimization.*;
import optic.lua.util.*;
import org.antlr.runtime.tree.*;
import org.jetbrains.annotations.*;
import org.slf4j.*;

import java.lang.String;
import java.util.*;

import static nl.bigo.luaparser.Lua53Walker.Name;
import static nl.bigo.luaparser.Lua53Walker.Not;
import static nl.bigo.luaparser.Lua53Walker.Number;
import static nl.bigo.luaparser.Lua53Walker.String;
import static nl.bigo.luaparser.Lua53Walker.*;
import static optic.lua.asm.ExprNode.*;

/**
 * Mutable implementation of a tree flattener. To flatten a given AST tree, use {@link #flatten(CommonTree, Options)}.
 * This implementation creates other flatteners recursively to flatten child blocks.
 */
public final class MutableFlattener implements VariableResolver {
	private static final Logger log = LoggerFactory.getLogger(MutableFlattener.class);
	// mutable list of current steps
	private final List<VoidNode> steps;
	// mutable container of current local variable
	private final Map<String, VariableInfo> locals = new HashMap<>(8);
	// parent scope
	private final MutableFlattener parent;
	// whether or not local variables from parent are accessed as upvalues
	private final boolean lexicalBoundary;
	// the one and only _ENV upvalue
	private final VariableInfo _ENV = VariableInfo.createEnv();
	private final Options options;
	// what kind of code block does
	private final BlockMeaning meaning;

	private MutableFlattener(List<VoidNode> steps, MutableFlattener parent, boolean boundary, Options options, BlockMeaning meaning) {
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
		var statements = Trees.childrenOf(tree);
		int expectedSize = statements.size() * 4 + 10;
		var f = new MutableFlattener(new ArrayList<>(expectedSize), parent, kind.hasLexicalBoundary(), context, kind);
		for (var local : locals) {
			local.markAsInitialized();
			f.locals.put(local.getName(), local);
		}
		for (var stat : statements) {
			int lineNumber = ((Tree) stat).getLine();
			if (lineNumber > 0) {
				f.steps.add(VoidNode.lineNumber(lineNumber));
			}
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
				List<VoidNode> steps = new ArrayList<>();
				var flattener = new MutableFlattener(steps, MutableFlattener.this, false, options, meaning);
				ListNode result = flattener.flattenExpression(tree);
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

	private AsmBlock flattenForRangeBody(CommonTree tree, StaticType counterType, String name) throws CompilationFailure {
		var info = new VariableInfo(name);
		info.update(counterType);
		return flatten(tree, options, this, List.of(info), BlockMeaning.LOOP_BODY);
	}

	private AsmBlock flattenFunctionBody(CommonTree tree, ParameterList params) throws CompilationFailure {
		var infos = new ArrayList<VariableInfo>(params.list().size());
		for (var param : params.list()) {
			infos.add(new VariableInfo(param));
		}
		return flatten(tree, options, this, infos, BlockMeaning.FUNCTION_BODY);
	}

	/**
	 * Recursively searches the parents of this flattener for a local variable with this name.
	 *
	 * @return An upvalue or local variable if one exists, otherwise null.
	 */
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

	@Contract(mutates = "this")
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
				var builder = new ChainedAccessBuilder(getInterface(), firstOnly(flattenExpression(t.getChild(0))));
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
				ExprNode from = evaluateOnce(ExprNode.toNumber(firstOnly(flattenExpression(t.getChild(1)))));
				ExprNode to = evaluateOnce(ExprNode.toNumber(firstOnly(flattenExpression(t.getChild(2)))));
				Tree stepOrBody = t.getChild(3);
				if (stepOrBody.getType() == Do) {
					// for loop without step:
					// for i = A, B do ... end
					CommonTree block = (CommonTree) stepOrBody.getChild(0);
					AsmBlock body = flattenForRangeBody(block, from.typeInfo(), varName);
					VariableInfo counter = body.locals().get(varName);
					steps.add(VoidNode.forRange(counter, from, to, body));
				} else {
					// for loop with step "C":
					// for i = A, B, C do ... end
					ExprNode step = evaluateOnce(ExprNode.toNumber(firstOnly(flattenExpression(stepOrBody))));
					CommonTree block = (CommonTree) t.getChild(4).getChild(0);
					AsmBlock body = flattenForRangeBody(block, from.typeInfo().and(step.typeInfo()), varName);
					VariableInfo counter = body.locals().get(varName);
					steps.add(VoidNode.forRange(counter, from, to, step, body));
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
				var iterator = evaluateOnce(firstOnly(flattenExpression(Trees.expectChild(EXPR_LIST, t, 1).getChild(0))));
				var body = flattenForInLoopBody((CommonTree) Trees.expectChild(Do, t, 2), variables);
				steps.add(VoidNode.forInLoop(variables, iterator, body));
				return;
			}
			case While: {
				// while (<condition>) (do (chunk (<body>)))
				var condition = getInterface().flattenExpression(t.getChild(0))
						.firstOnly()
						.mapValue(v -> ExprNode.toBoolean((ExprNode) v));
				var chunk = Trees.expect(CHUNK, t.getChild(1).getChild(0));
				var body = flattenLoopBody((CommonTree) chunk);
				var stepList = new ArrayList<VoidNode>(body.steps().size() + 8);
				stepList.addAll(condition.block());
				stepList.add(VoidNode.breakIf(firstOnly(condition.value()), false));
				stepList.addAll(body.steps());
				var processedBody = new AsmBlock(stepList, body.locals());
				steps.add(VoidNode.loop(processedBody));
				return;
			}
			case Repeat: {
				// repeat (chunk (<body>)) (<condition>)
				var condition = getInterface().flattenExpression(t.getChild(1))
						.firstOnly()
						.mapValue(v -> ExprNode.toBoolean((ExprNode) v));
				var chunk = Trees.expect(CHUNK, t.getChild(0));
				var body = flattenLoopBody((CommonTree) chunk);
				var stepList = new ArrayList<>(body.steps());
				stepList.addAll(condition.block());
				stepList.add(VoidNode.breakIf(firstOnly(condition.value()), true));
				var processedBody = new AsmBlock(stepList, body.locals());
				steps.add(VoidNode.loop(processedBody));
				return;
			}
			case Do: {
				CommonTree block = (CommonTree) t;
				steps.add(VoidNode.doBlock(flattenDoBlock(block)));
				return;
			}
			case CHUNK: {
				CommonTree block = (CommonTree) t;
				steps.addAll(flattenDoBlock(block).steps());
				return;
			}
			case Return: {
				List<ListNode> values = new ArrayList<>(t.getChildCount());
				for (Object o : Trees.childrenOf(t)) {
					Tree tree = (Tree) o;
					ListNode value = flattenExpression(tree);
					values.add(value);
				}
				steps.add(VoidNode.returnFromFunction(ListNode.exprList(values)));
				return;
			}
			case If: {
				var builder = new IfElseChainBuilder(getInterface());
				for (var x : Trees.childrenOf(t)) {
					builder.add((Tree) x);
				}
				steps.add(VoidNode.ifThenChain(builder.build()));
				return;
			}
			default: {
				log.error("Unknown statement: (type: {}) at line {}: {}", Trees.reverseLookupName(t.getType()), t.getLine(), t.toStringTree());
				throw new CompilationFailure();
			}
		}
	}

	@Contract(mutates = "this")
	private ListNode flattenExpression(Tree t) throws CompilationFailure {
		Objects.requireNonNull(t);
		if (Operators.isBinary(t)) {
			LuaOperator op = LuaOperator.forTokenType(t.getType());
			var a = firstOnly(flattenExpression(t.getChild(0)));
			var b = firstOnly(flattenExpression(t.getChild(1)));
			return ExprNode.monoInvocation(a, op.invocationMethod(), ListNode.exprList(b));
		}
		if (Operators.isUnary(t)) {
			LuaOperator op = LuaOperator.forTokenType(t.getType());
			ExprNode param = firstOnly(flattenExpression(t.getChild(0)));
			return ExprNode.monoInvocation(param, op.invocationMethod(), ListNode.exprList());
		}
		switch (t.getType()) {
			case Number: {
				return ExprNode.number(Double.parseDouble(t.getText()));
			}
			case String: {
				return ExprNode.string(t.getText());
			}
			case Name: {
				String name = t.getText();
				VariableInfo info = resolve(name);
				if (info == null) {
					var global = VariableInfo.global(name);
					return ExprNode.variableName(global);
				}
				return ExprNode.variableName(info);
			}
			case VAR: {
				var builder = new ChainedAccessBuilder(getInterface(), firstOnly(flattenExpression(t.getChild(0))));
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
				return ListNode.varargs();
			}
			case CONDITION: {
				return flattenExpression(t.getChild(0));
			}
			case Nil: {
				return ExprNode.nil();
			}
			case True: {
				return ExprNode.bool(true);
			}
			case False: {
				return ExprNode.bool(false);
			}
			case Or: {
				ExprNode a = evaluateOnce(firstOnly(flattenExpression(t.getChild(0))));
				ExprNode b = firstOnly(flattenExpression(t.getChild(1)));
				return ExprNode.logicalOr(a, b);
			}
			case And: {
				ExprNode a = evaluateOnce(firstOnly(flattenExpression(t.getChild(0))));
				ExprNode b = firstOnly(flattenExpression(t.getChild(1)));
				return ExprNode.logicalAnd(a, b);
			}
			case Not: {
				ExprNode x = evaluateOnce(toBoolean(firstOnly(flattenExpression(t.getChild(0)))));
				return ExprNode.logicalNot(x);
			}
			default: {
				log.error("Unknown expression: (type: {}) at line {}: {}", Trees.reverseLookupName(t.getType()), t.getLine(), t.toStringTree());
				throw new CompilationFailure();
			}
		}
	}

	@Contract(mutates = "this")
	private ExprNode createTableLiteral(Tree tree) throws CompilationFailure {
		Trees.expect(TABLE, tree);
		List<?> fields = Trees.childrenOf(tree);
		var builder = new TableLiteralBuilder(this.getInterface(), fields.size());
		for (Object obj : fields) {
			builder.addEntry((Tree) obj);
		}
		steps.addAll(builder.getSteps());
		return ExprNode.table(builder.getTable());
	}

	@Contract(mutates = "this")
	private ExprNode evaluateOnce(ExprNode value) {
		if (value.isPure()) {
			return value;
		}
		var register = Register.ofType(value::typeInfo);
		steps.add(VoidNode.assign(register, value));
		return register;
	}

	@Contract(mutates = "this")
	private ListNode.ExprList flattenAll(List<?> trees) throws CompilationFailure {
		int size = trees.size();
		var list = new ArrayList<ListNode>(size);
		for (Object tree : trees) {
			list.add(flattenExpression((Tree) tree));
		}
		return ListNode.exprList(list);
	}

	@Contract(mutates = "this")
	private LValue createLValue(Object name) throws CompilationFailure {
		if (name instanceof Tree && ((Tree) name).getType() == ASSIGNMENT_VAR) {
			// table assignment
			var t = (CommonTree) name;

			var builder = new ChainedAccessBuilder(getInterface(), firstOnly(flattenExpression(t.getChild(0))));
			for (int i = 1, size = t.getChildCount(); i < size; i++) {
				builder.add(t.getChild(i));
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
		ExprList values = flattenAll(Trees.childrenOf(valueList));
		builder.setValues(values);
		List<?> names = Trees.childrenOf(nameList);

		// if the assignment starts with local, no need to worry about table assignments
		// this code is illegal: "local a, tb[k] = 4, 2"
		for (var name : names) {
			var info = resolve(name.toString());
			if (info == null || info.getMode() != VariableMode.LOCAL) {
				// if variable doesn't exist yet
				if (local) {
					// declare the variable
					var variable = new VariableInfo(name.toString());
					locals.put(name.toString(), variable);
					declare(variable);
				}
			} else if (locals.containsKey(info.getName()) && options.get(StandardFlags.SSA_SPLIT)) {
				// if variable is a local variable from *this* scope and is reassigned
				// clear it's type information by replacing it with a fresh one
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
		VoidNode step = VoidNode.declareLocal(variable);
		steps.add(step);
	}

	@Contract(pure = true)
	private ExprNode createFunctionLiteral(Tree t) throws CompilationFailure {
		Trees.expect(FUNCTION, t);
		Tree paramList = Trees.expectChild(PARAM_LIST, t, 0);
		var params = ParameterList.parse(((CommonTree) paramList));
		Tree chunk = Trees.expectChild(CHUNK, t, 1);
		AsmBlock body = flattenFunctionBody((CommonTree) chunk, params);
		return ExprNode.function(params, body);
	}
}
