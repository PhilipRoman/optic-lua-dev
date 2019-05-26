package optic.lua.codegen.java;

import optic.lua.asm.*;
import optic.lua.asm.RValue.*;
import optic.lua.codegen.ResultBuffer;
import optic.lua.messages.*;
import optic.lua.optimization.*;
import optic.lua.util.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.*;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

final class JavaExpressionVisitor implements RValueVisitor<String, CompilationFailure> {
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
	public String visitNumberConstant(double n) {
		return Numbers.isInt(n) ? Long.toString((long) n) : Double.toString(n);
	}

	@Override
	public String visitStringConstant(String s) {
		return '"' + StringUtils.escape(s) + '"';
	}

	@Override
	public String visitBooleanConstant(boolean b) {
		return b ? "true" : "false";
	}

	@Override
	public String visitNilConstant() {
		return "null";
	}

	@Override
	public String visitNot(RValue value) throws CompilationFailure {
		if (value.typeInfo().isNumeric()) {
			return "Boolean.FALSE";
		}
		String valueExpr = value.accept(this);
		return String.format("((%s) == null || (%s) == Boolean.FALSE)", valueExpr, valueExpr);
	}

	@Override
	public String visitAnd(RValue first, RValue second) throws CompilationFailure {
		if (first.typeInfo().isNumeric()) { // all numeric values are 'true'
			return second.accept(this);
		}
		String firstExpr = first.accept(this);
		String secondExpr = second.accept(this);
		String commonType = JavaUtils.typeName(first.typeInfo().and(second.typeInfo()));
		return String.format("((Object)(%s) == null || (Object)(%s) == Boolean.FALSE ? (%s)(%s) : (%s)(%s))",
				firstExpr, firstExpr,
				commonType, firstExpr,
				commonType, secondExpr);
	}

	@Override
	public String visitOr(RValue first, RValue second) throws CompilationFailure {
		if (first.typeInfo().isNumeric()) { // all numeric values are 'true'
			return first.accept(this);
		}
		String firstExpr = first.accept(this);
		String secondExpr = second.accept(this);
		String commonType = JavaUtils.typeName(first.typeInfo().and(second.typeInfo()));
		return String.format("((Object)(%s) == null || (Object)(%s) == Boolean.FALSE ? (%s)(%s) : (%s)(%s))",
				firstExpr, firstExpr,
				commonType, secondExpr,
				commonType, firstExpr);
	}

	@Override
	public String visitTableConstructor(TableLiteral t) throws CompilationFailure {
		var map = new LinkedHashMap<>(t.entries());
		Optional<Entry<RValue, RValue>> vararg = map.entrySet().stream()
				.filter(e -> e.getValue().isVararg())
				.findAny();
		// vararg is treated separately, remove it from the map
		vararg.ifPresent(v -> map.remove(v.getKey()));
		StringJoiner joiner = new StringJoiner(", ");
		for (Entry<RValue, RValue> e : map.entrySet()) {
			joiner.add(e.getKey().accept(this));
			joiner.add(e.getValue().accept(this));
		}
		String list = joiner.toString();
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
			return creationSiteName + ".createWithVararg(" + offset + ", " + value + (map.isEmpty() ? "" : ", ") + list + ")";
		} else {
			return creationSiteName + ".create(" + list + ")";
		}
	}

	@Override
	public String visitFunctionLiteral(FunctionLiteral t) throws CompilationFailure {
		ResultBuffer buffer = new ResultBuffer();
		var params = t.parameters().list();
		var argsName = "args" + UniqueNames.next();
		String functionCreationSiteName = "function_factory_" + UniqueNames.next();
		statementVisitor.addConstant(
				"FunctionFactory",
				functionCreationSiteName,
				nestedData.rootContextName() + ".functionFactory(" + idCounter.incrementAndGet() + ")");
		var contextName = nestedData.pushNewContextName();
		buffer.add("new LuaFunction(", functionCreationSiteName, "){ Object[] call(LuaContext " + contextName + ", Object[] " + argsName + ") { if(1==1) {");
		for (var p : params) {
			if (p.equals("...")) {
				var varargName = nestedData.pushNewVarargName();
				int offset = params.size() - 1;
				buffer.add("\tfinal Object[] " + varargName + " = sublist(" + argsName + ", " + offset + ");");
			} else {
				var param = t.body().locals().get(p);
				Objects.requireNonNull(param);
				boolean isUpValue = param.getMode() == VariableMode.UPVALUE;
				var paramTypeName = (isUpValue && !param.isFinal()) ? "UpValue" : "Object";
				String finalPrefix = param.isFinal() ? "final " : "";
				buffer.add(finalPrefix + paramTypeName + " " + LOCAL_VARIABLE_PREFIX + p + " = get(" + argsName + ", " + params.indexOf(p) + ");");
			}
		}
		if (!t.parameters().hasVarargs()) {
			nestedData.pushMissingVarargName();
		}

		buffer.addBlock(statementVisitor.visitAll(t.body().steps()));
		buffer.add("} return EMPTY; }}");
		nestedData.popLastContextName();
		nestedData.popLastVarargName();
		var out = new ByteArrayOutputStream(256);
		buffer.writeTo(new PrintStream(out), options().get(Option.INDENT));
		return out.toString();
	}

	@Override
	public String visitRegister(Register r) {
		return r.name();
	}

	@Override
	public String visitLocalName(VariableInfo variable) {
		return LOCAL_VARIABLE_PREFIX + variable.getName();
	}

	@Override
	public String visitUpValueName(VariableInfo upvalue) {
		if (upvalue.isEnv()) {
			return nestedData.contextName() + "._ENV";
		}
		if (upvalue.isFinal()) {
			return LOCAL_VARIABLE_PREFIX + upvalue.getName();
		}
		return LOCAL_VARIABLE_PREFIX + upvalue.getName() + ".value";
	}

	@Override
	public String visitGlobalName(VariableInfo global) {
		String debugComment = options().get(StandardFlags.DEBUG_COMMENTS) ? " /* " + global.toDebugString() + " */" : "";
		String contextName = nestedData.contextName();
		return contextName + ".getGlobal(\"" + global.getName() + "\")" + debugComment;
	}

	@Override
	public String visitInvocation(@NotNull Invocation x) throws CompilationFailure {
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
	public String visitVarargs() throws CompilationFailure {
		if (options().get(StandardFlags.ALLOW_UPVALUE_VARARGS)) {
			return nestedData.firstNestedVarargName().orElseThrow(this::varargError);
		}
		return nestedData.varargName().orElseThrow(this::varargError);
	}

	private CompilationFailure varargError() {
		log.error("Cannot use \"...\" outside of vararg context");
		return new CompilationFailure();
	}

	private String compileToNumber(RValue value) throws CompilationFailure {
		return "toNum(" + value.accept(this) + ")";
	}

	private String compileBinaryOperatorInvocation(LuaOperator op, RValue a, RValue b) throws CompilationFailure {
		if (JavaOperators.canApplyJavaSymbol(op, a.typeInfo(), b.typeInfo())) {
			String javaOp = Objects.requireNonNull(JavaOperators.javaSymbol(op));
			if (op == LuaOperator.DIV && a.typeInfo() == ProvenType.INTEGER && b.typeInfo() == ProvenType.INTEGER) {
				// special case to avoid integer division (in Lua all division is floating-point)
				return a.accept(this) + " " + javaOp + " (double) " + b.accept(this);
			}
			return a.accept(this) + " " + javaOp + " " + b.accept(this);
		}
		// if there is no corresponding Java operator, call the runtime API
		String function = op.name().toLowerCase();
		var context = nestedData.contextName();
		return function + "(" + context + ", " + a.accept(this) + ", " + b.accept(this) + ")";
	}

	private String compileUnaryOperatorInvocation(LuaOperator op, RValue value) throws CompilationFailure {
		if (JavaOperators.canApplyJavaSymbol(op, null, value.typeInfo())) {
			String javaOp = Objects.requireNonNull(JavaOperators.javaSymbol(op));
			return javaOp + " " + value.accept(this);
		}
		// if there is no corresponding Java operator, call the runtime API
		String function = op.name().toLowerCase();
		var context = nestedData.contextName();
		return function + "(" + context + ", " + value.accept(this) + ")";
	}

	private String compileTableWrite(RValue table, RValue key, RValue value) throws CompilationFailure {
		return "setIndex(" + table.accept(this) + ", " + key.accept(this) + ", " + value.accept(this) + ")";
	}

	private String compileTableRead(RValue table, RValue key) throws CompilationFailure {
		return "index(" + table.accept(this) + ", " + key.accept(this) + ")";
	}

	private String compileFunctionCall(RValue function, List<RValue> arguments) throws CompilationFailure {
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
				? "append(" + commaList(argList) + ")"
				: "list(" + commaList(argList) + ")";
		return callSiteName + ".invoke(" + contextName + ", " + function.accept(this) + "," + args + ")";
	}

	private CharSequence commaList(List<RValue> args) throws CompilationFailure {
		int size = args.size();
		var builder = new StringBuilder(size * 5 + 10);
		for (int i = 0; i < size; i++) {
			builder.append(args.get(i).accept(this));
			if (i != size - 1) {
				builder.append(", ");
			}
		}
		return builder;
	}

	private void logIllegalVarargUsageError() {
		log.error("Cannot use ... outside of vararg function");
	}
}
