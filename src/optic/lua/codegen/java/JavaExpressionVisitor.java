package optic.lua.codegen.java;

import optic.lua.asm.*;
import optic.lua.asm.ExprNode.*;
import optic.lua.codegen.*;
import optic.lua.messages.*;
import optic.lua.optimization.*;
import optic.lua.util.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.*;

import java.text.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

final class JavaExpressionVisitor implements ExpressionVisitor<ResultBuffer, CompilationFailure> {
	private static final Logger log = LoggerFactory.getLogger(JavaExpressionVisitor.class);
	private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat();

	static {
		var symbols = DecimalFormatSymbols.getInstance();
		symbols.setDecimalSeparator('.');
		NUMBER_FORMAT.setDecimalFormatSymbols(symbols);
		NUMBER_FORMAT.setGroupingUsed(false);
		NUMBER_FORMAT.setMaximumFractionDigits(340); // DecimalFormat.DOUBLE_FRACTION_DIGITS
		NUMBER_FORMAT.setMinimumFractionDigits(1);
		NUMBER_FORMAT.setMinimumIntegerDigits(1);
	}

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
		return Line.of(Numbers.isInt(n)
				? ((long) n) + "L"
				: NUMBER_FORMAT.format(n) + "d");
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
	public ResultBuffer visitNot(ExprNode value) throws CompilationFailure {
		var valueExpr = value.accept(this);
		return Line.join("!(", valueExpr, ")");
	}

	@Override
	public ResultBuffer visitAnd(ExprNode first, ExprNode second) throws CompilationFailure {
		if (first.typeInfo().isNumeric()) { // all numeric values are 'true'
			return second.accept(this);
		}
		var firstExpr = first.accept(this);
		var secondExpr = second.accept(this);
		String commonType = JavaUtils.typeName(first.typeInfo().and(second.typeInfo()));
		return Line.join("(toBool(", firstExpr, ") ",
				"? (", commonType, ")(", secondExpr, ") ",
				": (", commonType, ")(", firstExpr, "))");
	}

	@Override
	public ResultBuffer visitOr(ExprNode first, ExprNode second) throws CompilationFailure {
		if (first.typeInfo().isNumeric()) { // all numeric values are 'true'
			return first.accept(this);
		}
		var firstExpr = first.accept(this);
		var secondExpr = second.accept(this);
		String commonType = JavaUtils.typeName(first.typeInfo().and(second.typeInfo()));
		return Line.join("(toBool(", firstExpr, ") ",
				"? (", commonType, ")(", firstExpr, ") ",
				": (", commonType, ")(", secondExpr, "))");
	}

	@Override
	public ResultBuffer visitSelectNth(ListNode source, int n) throws CompilationFailure {
		return Line.join("get(", source.accept(this), ", ", n, ")");
	}

	@Override
	public ResultBuffer visitExprList(List<ExprNode> leading, Optional<ListNode> trailing) throws CompilationFailure {
		if (trailing.isPresent()) {
			return Line.join("append(", trailing.get().accept(this), leading.isEmpty() ? "" : ", ", commaList(leading), ")");
		}
		return Line.join("list(", commaList(leading), ")");
	}

	@Override
	public ResultBuffer acceptIntrinsic(String methodName, ExprList args) throws CompilationFailure {
		return Line.join(methodName, "(", commaList(args.getLeading()), ")");
	}

	@Override
	public ResultBuffer visitTableConstructor(TableLiteral t) throws CompilationFailure {
		var copy = new LinkedHashMap<>(t.entries());
		Entry<ExprNode, ListNode> vararg = copy.entrySet().stream()
				.filter(e -> e.getValue().isVararg())
				.findAny()
				.orElse(null);
		if (vararg != null && vararg.getKey().typeInfo() != StaticType.INTEGER)
			throw new InternalCompilerError("Vararg key must be an integer");
		// vararg is treated separately, remove it from the map
		if (vararg != null)
			copy.remove(vararg.getKey());
		var joiner = new ArrayList<ExprNode>(copy.size() * 2);
		for (Entry<ExprNode, ListNode> e : copy.entrySet()) {
			joiner.add(e.getKey());
			joiner.add((ExprNode) e.getValue());
		}
		ResultBuffer list = commaList(joiner);
		if (vararg != null) {
			var offset = vararg.getKey().accept(this);
			var varargValue = vararg.getValue().accept(this);
			return Line.join("varargTable(", offset, ", ", varargValue, (copy.isEmpty() ? "" : ", "), list, ")");
		} else {
			return Line.join("table(", list, ")");
		}
	}

	@Override
	public ResultBuffer visitFunctionLiteral(FunctionLiteral f) throws CompilationFailure {
		LineList buffer = new LineList();
		var params = f.parameters().list();
		var argsName = "args" + UniqueNames.next();
		String functionCreationSiteName = "function_factory_" + UniqueNames.next();
		statementVisitor.addConstant(
				"FunctionFactory",
				functionCreationSiteName,
				nestedData.rootContextName() + ".functionFactory(" + idCounter.incrementAndGet() + ")");
		var contextName = nestedData.pushNewContextName();
		buffer.addLine("new LuaFunction(", functionCreationSiteName, "){ public Object[] call(LuaContext " + contextName + ", Object[] " + argsName + ") { if(1==1) {");
		for (var p : params) {
			if (p.equals("...")) {
				var varargName = nestedData.pushNewVarargName();
				int offset = params.size() - 1;
				buffer.addLine("\tfinal Object[] ", varargName, " = sublist(", argsName, ", ", offset, ");");
			} else {
				var param = f.body().locals().get(p);
				Objects.requireNonNull(param);
				boolean isUpValue = param.getMode() == VariableMode.UPVALUE;
				var paramTypeName = (isUpValue && !param.isFinal()) ? "UpValue" : "Object";
				String finalPrefix = param.isFinal() ? "final " : "";
				buffer.addLine(finalPrefix, paramTypeName, " ", LOCAL_VARIABLE_PREFIX, p, " = get(", argsName, ", ", params.indexOf(p), ");");
			}
		}
		if (!f.parameters().hasVarargs()) {
			nestedData.pushMissingVarargName();
		}

		buffer.addAllChildren(statementVisitor.visitAll(f.body().steps()));
		buffer.addLine("} return EMPTY; }}");
		nestedData.popLastContextName();
		nestedData.popLastVarargName();
		return buffer;
	}

	@Override
	public ResultBuffer visitRegister(Register register) {
		return Line.of(register.name());
	}

	@Override
	public ResultBuffer visitArrayRegister(ArrayRegister register) {
		return Line.of(register.name());
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
	public ResultBuffer visitInvocation(@NotNull ListNode.Invocation x) throws CompilationFailure {
		var args = x.getArguments();
		switch (x.getMethod()) {
			case CALL:
				return compileFunctionCall(x.getObject(), args);
			case INDEX:
				return compileTableRead(x.getObject(), args.getLeading(0));
			case SET_INDEX:
				return compileTableWrite(x.getObject(), args.getLeading(0), args.getLeading(1));
			case TO_NUMBER:
				return compileToNumber(x.getObject());
			case TO_BOOLEAN:
				return compileToBoolean(x.getObject());
			default:
				var operator = LuaOperator.valueOf(x.getMethod().name());
				var first = x.getObject();
				if (operator.arity() == 2) {
					var second = args.getLeading(0);
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

	private ResultBuffer compileToNumber(ExprNode value) throws CompilationFailure {
		return Line.join("toNum(", value.accept(this), ")");
	}

	private ResultBuffer compileToBoolean(ExprNode value) throws CompilationFailure {
		return Line.join("toBool(", value.accept(this), ")");
	}

	private ResultBuffer compileBinaryOperatorInvocation(LuaOperator op, ExprNode a, ExprNode b) throws CompilationFailure {
		if (JavaOperators.canApplyJavaSymbol(op, a.typeInfo(), b.typeInfo())) {
			String javaOp = Objects.requireNonNull(JavaOperators.javaSymbol(op));
			if (op == LuaOperator.DIV && a.typeInfo() == StaticType.INTEGER && b.typeInfo() == StaticType.INTEGER) {
				// special case to avoid integer division (in Lua all division is floating-point)
				return Line.join(a.accept(this), " ", javaOp, " (double) ", b.accept(this));
			}
			return Line.join("(", a.accept(this), " ", javaOp, " ", b.accept(this), ")");
		}
		// if there is no corresponding Java operator, call the runtime API
		String function = op.name().toLowerCase();
		var context = nestedData.contextName();
		return Line.join(function, "(", context, ", ", a.accept(this), ", ", b.accept(this), ")");
	}

	private ResultBuffer compileUnaryOperatorInvocation(LuaOperator op, ExprNode value) throws CompilationFailure {
		if (JavaOperators.canApplyJavaSymbol(op, null, value.typeInfo())) {
			String javaOp = Objects.requireNonNull(JavaOperators.javaSymbol(op));
			return Line.join(javaOp, " ", value.accept(this));
		}
		// if there is no corresponding Java operator, call the runtime API
		String function = op.name().toLowerCase();
		var context = nestedData.contextName();
		return Line.join(function, "(", context, ", ", value.accept(this), ")");
	}

	private ResultBuffer compileTableWrite(ExprNode table, ExprNode key, ExprNode value) throws CompilationFailure {
		if (table.typeInfo() == StaticType.TABLE)
			return Line.join(table.accept(this), ".set(", key.accept(this), ", (Object) (", value.accept(this), "))");
		return Line.join("setIndex(", table.accept(this), ", ", key.accept(this), ", ", value.accept(this), ")");
	}

	private ResultBuffer compileTableRead(ExprNode table, ExprNode key) throws CompilationFailure {
		if (table.typeInfo() == StaticType.TABLE)
			return Line.join(table.accept(this), ".get(", key.accept(this), ")");
		return Line.join("index(", table.accept(this), ", ", key.accept(this), ")");
	}

	private ResultBuffer compileFunctionCall(ExprNode function, ListNode arguments) throws CompilationFailure {
		var contextName = nestedData.contextName();
		if (function.typeInfo() == StaticType.FUNCTION) {
			return Line.join(function.accept(this), ".call(", contextName, ", ", arguments.accept(this), ")");
		}
		String callSiteName = "call_site_" + UniqueNames.next();
		statementVisitor.addConstant(
				"CallSite",
				callSiteName,
				nestedData.rootContextName() + ".callSite(" + idCounter.incrementAndGet() + ")"
		);
		return Line.join(callSiteName, ".invoke(", contextName, ", ", function.accept(this), ",", arguments.accept(this), ")");

	}

	private ResultBuffer commaList(List<ExprNode> args) throws CompilationFailure {
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
