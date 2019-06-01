package optic.lua.codegen.java;

import optic.lua.asm.*;
import optic.lua.asm.RValue.*;
import optic.lua.codegen.*;
import optic.lua.messages.*;
import optic.lua.optimization.*;
import optic.lua.util.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.*;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

final class JavaExpressionVisitor implements RValueVisitor<ResultBuffer, CompilationFailure> {
	private static final Logger log = LoggerFactory.getLogger(JavaExpressionVisitor.class);
	static final String LOCAL_VARIABLE_PREFIX = "L_";
	private static AtomicInteger idCounter = new AtomicInteger();
	private final NestedData nestedData;
	private final JavaCodeOutput statementVisitor;

	JavaExpressionVisitor(NestedData data, JavaCodeOutput visitor) {
		nestedData = Objects.requireNonNull(data);
		statementVisitor = visitor;
	}

	private Options options() {
		return statementVisitor.options;
	}

	@Override
	public ResultBuffer visitNumberConstant(double n) {
		return Line.of(Numbers.isInt(n) ? Long.toString((long) n) : Double.toString(n));
	}

	@Override
	public ResultBuffer visitStringConstant(String s) {
		return Line.of('"' + StringUtils.escape(s) + '"');
	}

	@Override
	public ResultBuffer visitBooleanConstant(boolean b) {
		return Line.of(b ? "true" : "false");
	}

	@Override
	public ResultBuffer visitNilConstant() {
		return Line.of("null");
	}

	@Override
	public ResultBuffer visitNot(RValue value) throws CompilationFailure {
		if (value.typeInfo().isNumeric()) {
			return Line.of("Boolean.FALSE");
		}
		var valueExpr = value.accept(this);
		return Line.join("((", valueExpr, ") == null || (", valueExpr, ") == Boolean.FALSE)");
	}

	@Override
	public ResultBuffer visitAnd(RValue first, RValue second) throws CompilationFailure {
		if (first.typeInfo().isNumeric()) { // all numeric values are 'true'
			return second.accept(this);
		}
		var firstExpr = first.accept(this);
		var secondExpr = second.accept(this);
		String commonType = JavaUtils.typeName(first.typeInfo().and(second.typeInfo()));
		return Line.join("((Object)(", firstExpr, ") == null || (Object)(", firstExpr, ") == Boolean.FALSE ",
				"? (", commonType, ")(", firstExpr, ") ",
				": (", commonType, ")(", secondExpr, "))");
	}

	@Override
	public ResultBuffer visitOr(RValue first, RValue second) throws CompilationFailure {
		if (first.typeInfo().isNumeric()) { // all numeric values are 'true'
			return first.accept(this);
		}
		var firstExpr = first.accept(this);
		var secondExpr = second.accept(this);
		String commonType = JavaUtils.typeName(first.typeInfo().and(second.typeInfo()));
		return Line.join("((Object)(", firstExpr, ") == null || (Object)(", firstExpr, ") == Boolean.FALSE ",
				"? (", commonType, ")(", secondExpr, ") ",
				": (", commonType, ")(", firstExpr, "))");
	}

	@Override
	public ResultBuffer visitTableConstructor(TableLiteral t) throws CompilationFailure {
		var map = new LinkedHashMap<>(t.entries());
		Optional<Entry<RValue, RValue>> vararg = map.entrySet().stream()
				.filter(e -> e.getValue().isVararg())
				.findAny();
		// vararg is treated separately, remove it from the map
		vararg.ifPresent(v -> map.remove(v.getKey()));
		var joiner = new ArrayList<RValue>(map.size() * 2);
		for (Entry<RValue, RValue> e : map.entrySet()) {
			joiner.add(e.getKey());
			joiner.add(e.getValue());
		}
		ResultBuffer list = commaList(joiner);
		String creationSiteName = "table_factory_" + UniqueNames.next();
		statementVisitor.addConstant(
				"TableFactory",
				creationSiteName,
				nestedData.rootContextName() + ".tableFactory(" + idCounter.incrementAndGet() + ")"
		);
		if (vararg.isPresent()) {
			var v = vararg.get();
			var offset = v.getKey().accept(this);
			var value = v.getValue().accept(this);
			return Line.join(creationSiteName, ".createWithVararg(", offset, ", ", value, (map.isEmpty() ? "" : ", "), list, ")");
		} else {
			return Line.join(creationSiteName, ".create(", list, ")");
		}
	}

	@Override
	public ResultBuffer visitFunctionLiteral(FunctionLiteral t) throws CompilationFailure {
		LineList buffer = new LineList();
		var params = t.parameters().list();
		var argsName = "args" + UniqueNames.next();
		String functionCreationSiteName = "function_factory_" + UniqueNames.next();
		statementVisitor.addConstant(
				"FunctionFactory",
				functionCreationSiteName,
				nestedData.rootContextName() + ".functionFactory(" + idCounter.incrementAndGet() + ")");
		var contextName = nestedData.pushNewContextName();
		buffer.addLine("new LuaFunction(", functionCreationSiteName, "){ Object[] call(LuaContext " + contextName + ", Object[] " + argsName + ") { if(1==1) {");
		for (var p : params) {
			if (p.equals("...")) {
				var varargName = nestedData.pushNewVarargName();
				int offset = params.size() - 1;
				buffer.addLine("\tfinal Object[] ", varargName, " = sublist(", argsName, ", ", offset, ");");
			} else {
				var param = t.body().locals().get(p);
				Objects.requireNonNull(param);
				boolean isUpValue = param.getMode() == VariableMode.UPVALUE;
				var paramTypeName = (isUpValue && !param.isFinal()) ? "UpValue" : "Object";
				String finalPrefix = param.isFinal() ? "final " : "";
				buffer.addLine(finalPrefix, paramTypeName, " ", LOCAL_VARIABLE_PREFIX, p, " = get(", argsName, ", ", params.indexOf(p), ");");
			}
		}
		if (!t.parameters().hasVarargs()) {
			nestedData.pushMissingVarargName();
		}

		buffer.addAllChildren(statementVisitor.visitAll(t.body().steps()));
		buffer.addLine("} return EMPTY; }}");
		nestedData.popLastContextName();
		nestedData.popLastVarargName();
		return buffer;
	}

	@Override
	public ResultBuffer visitRegister(Register r) {
		return Line.of(r.name());
	}

	@Override
	public ResultBuffer visitLocalName(VariableInfo variable) {
		return Line.of(LOCAL_VARIABLE_PREFIX + variable.getName());
	}

	@Override
	public ResultBuffer visitUpValueName(VariableInfo upvalue) {
		if (upvalue.isEnv()) {
			return Line.of(nestedData.contextName() + "._ENV");
		}
		if (upvalue.isFinal()) {
			return Line.of(LOCAL_VARIABLE_PREFIX + upvalue.getName());
		}
		return Line.of(LOCAL_VARIABLE_PREFIX + upvalue.getName() + ".value");
	}

	@Override
	public ResultBuffer visitGlobalName(VariableInfo global) {
		String debugComment = options().get(StandardFlags.DEBUG_COMMENTS) ? " /* " + global.toDebugString() + " */" : "";
		String contextName = nestedData.contextName();
		return Line.join(contextName, ".getGlobal(\"", global.getName(), "\")", debugComment);
	}

	@Override
	public ResultBuffer visitInvocation(@NotNull Invocation x) throws CompilationFailure {
		switch (x.getMethod()) {
			case CALL:
				return compileFunctionCall(x.getObject(), x.getArguments());
			case INDEX:
				return compileTableRead(x.getObject(), x.getArguments().get(0));
			case SET_INDEX:
				return compileTableWrite(x.getObject(), x.getArguments().get(0), x.getArguments().get(1));
			case TO_NUMBER:
				return compileToNumber(x.getObject());
			default:
				var operator = LuaOperator.valueOf(x.getMethod().name());
				var first = x.getObject();
				if (operator.arity() == 2) {
					var second = x.getArguments().get(0);
					return compileBinaryOperatorInvocation(operator, first, second);
				} else {
					return compileUnaryOperatorInvocation(operator, first);
				}
		}
	}

	@Override
	public ResultBuffer visitVarargs() throws CompilationFailure {
		if (options().get(StandardFlags.ALLOW_UPVALUE_VARARGS)) {
			return Line.of(nestedData.firstNestedVarargName().orElseThrow(this::varargError));
		}
		return Line.of(nestedData.varargName().orElseThrow(this::varargError));
	}

	private CompilationFailure varargError() {
		log.error("Cannot use \"...\" outside of vararg context");
		return new CompilationFailure();
	}

	private ResultBuffer compileToNumber(RValue value) throws CompilationFailure {
		return Line.join("toNum(", value.accept(this), ")");
	}

	private ResultBuffer compileBinaryOperatorInvocation(LuaOperator op, RValue a, RValue b) throws CompilationFailure {
		if (JavaOperators.canApplyJavaSymbol(op, a.typeInfo(), b.typeInfo())) {
			String javaOp = Objects.requireNonNull(JavaOperators.javaSymbol(op));
			if (op == LuaOperator.DIV && a.typeInfo() == ProvenType.INTEGER && b.typeInfo() == ProvenType.INTEGER) {
				// special case to avoid integer division (in Lua all division is floating-point)
				return Line.join(a.accept(this), " ", javaOp, " (double) ", b.accept(this));
			}
			return Line.join(a.accept(this), " ", javaOp, " ", b.accept(this));
		}
		// if there is no corresponding Java operator, call the runtime API
		String function = op.name().toLowerCase();
		var context = nestedData.contextName();
		return Line.join(function, "(", context, ", ", a.accept(this), ", ", b.accept(this), ")");
	}

	private ResultBuffer compileUnaryOperatorInvocation(LuaOperator op, RValue value) throws CompilationFailure {
		if (JavaOperators.canApplyJavaSymbol(op, null, value.typeInfo())) {
			String javaOp = Objects.requireNonNull(JavaOperators.javaSymbol(op));
			return Line.join(javaOp, " ", value.accept(this));
		}
		// if there is no corresponding Java operator, call the runtime API
		String function = op.name().toLowerCase();
		var context = nestedData.contextName();
		return Line.join(function, "(", context, ", ", value.accept(this), ")");
	}

	private ResultBuffer compileTableWrite(RValue table, RValue key, RValue value) throws CompilationFailure {
		return Line.join("setIndex(", table.accept(this), ", ", key.accept(this), ", ", value.accept(this), ")");
	}

	private ResultBuffer compileTableRead(RValue table, RValue key) throws CompilationFailure {
		return Line.join("index(", table.accept(this), ", ", key.accept(this), ")");
	}

	private ResultBuffer compileFunctionCall(RValue function, List<RValue> arguments) throws CompilationFailure {
		var argList = new ArrayList<>(arguments);
		String callSiteName = "call_site_" + UniqueNames.next();
		statementVisitor.addConstant(
				"CallSite",
				callSiteName,
				nestedData.rootContextName() + ".callSite(" + idCounter.incrementAndGet() + ")"
		);
		boolean isVararg = !argList.isEmpty() && argList.get(argList.size() - 1).isVararg();
		if (isVararg) {
			// put the last element in the first position
			argList.add(0, argList.remove(argList.size() - 1));
		}
		var contextName = nestedData.contextName();
		var args = isVararg
				? Line.join("append(", commaList(argList), ")")
				: Line.join("list(", commaList(argList), ")");
		return Line.join(callSiteName, ".invoke(", contextName, ", ", function.accept(this), ",", args, ")");
	}

	ResultBuffer commaList(List<RValue> args) throws CompilationFailure {
		if (args.isEmpty()) {
			return Line.of("");
		}
		var builder = new ArrayList<>(args.size() * 2);
		int size = args.size();
		for (int i = 0; i < size; i++) {
			builder.add(args.get(i).accept(this));
			if (i != size - 1) {
				builder.add(", ");
			}
		}
		return Line.join(builder.toArray());
	}

}
