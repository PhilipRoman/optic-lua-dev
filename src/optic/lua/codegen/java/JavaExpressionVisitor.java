package optic.lua.codegen.java;

import optic.lua.asm.*;
import optic.lua.asm.RValue.*;
import optic.lua.codegen.ResultBuffer;
import optic.lua.messages.*;
import optic.lua.optimization.*;
import optic.lua.util.*;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

class JavaExpressionVisitor implements RValueVisitor<String, CompilationFailure> {
	private final NestedData nestedData;
	private final JavaCodeOutput statementVisitor;

	JavaExpressionVisitor(NestedData data, JavaCodeOutput visitor) {
		nestedData = Objects.requireNonNull(data);
		statementVisitor = visitor;
	}

	private Options options() {
		return statementVisitor.context.options();
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
		if (vararg.isPresent()) {
			var v = vararg.get();
			var offset = v.getKey();
			var value = v.getValue().accept(this);
			return "TableOps.createWithVararg(" + offset + ", " + value + ", " + list + ")";
		} else {
			return "TableOps.create(" + list + ")";
		}
	}

	@Override
	public String visitFunctionLiteral(FunctionLiteral t) throws CompilationFailure {
		ResultBuffer buffer = new ResultBuffer();
		var params = t.parameters().list();
		var argsName = "args" + UniqueNames.next();
		var contextName = nestedData.pushNewContextName();
		buffer.add("new LuaFunction(){ Object[] call(LuaContext " + contextName + ", Object[] " + argsName + ") { if(1==1) {");
		for (var p : params) {
			if (p.equals("...")) {
				var varargName = nestedData.pushNewVarargName();
				int offset = params.size() - 1;
				buffer.add("\tfinal Object[] " + varargName + " = ListOps.sublist(" + argsName + ", " + offset + ");");
			} else {
				var param = t.body().locals().get(p);
				Objects.requireNonNull(param);
				boolean isUpValue = param.getMode() == VariableMode.UPVALUE;
				var paramTypeName = (isUpValue && !param.isFinal()) ? "UpValue" : "Object";
				String finalPrefix = param.isFinal() ? "final " : "";
				buffer.add(finalPrefix + paramTypeName + " " + p + " = ListOps.get(" + argsName + ", " + params.indexOf(p) + ");");
			}
		}
		if (!t.parameters().hasVarargs()) {
			nestedData.pushMissingVarargName();
		}

		buffer.addBlock(statementVisitor.visitAll(t.body().steps()));
		buffer.add("} return ListOps.empty(); }}");
		nestedData.popLastContextName();
		nestedData.popLastVarargName();
		var out = new ByteArrayOutputStream(256);
		buffer.writeTo(new PrintStream(out), options().get(Option.INDENT));
		return out.toString();
	}

	@Override
	public String visitRegister(Register r) {
		if (options().get(StandardFlags.DEBUG_COMMENTS)) {
			return r.getName() + " /* " + r.toDebugString() + " */";
		} else {
			return r.getName();
		}
	}

	@Override
	public String visitLocalName(VariableInfo variable) {
		if (options().get(StandardFlags.DEBUG_COMMENTS)) {
			return variable.getName() + " /* " + variable.toDebugString() + " */";
		} else {
			return variable.getName();
		}
	}

	@Override
	public String visitUpValueName(VariableInfo upvalue) {
		String debugComment = options().get(StandardFlags.DEBUG_COMMENTS) ? " /* " + upvalue.toDebugString() + " */" : "";
		if (upvalue.isEnv()) {
			return nestedData.contextName() + "._ENV" + debugComment;
		}
		if (upvalue.isFinal()) {
			return upvalue.getName() + debugComment;
		}
		return upvalue.getName() + ".get()" + debugComment;
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
			return nestedData.firstNestedVarargName().orElseThrow(this::illegalVarargUsage);
		}
		return nestedData.varargName().orElseThrow(this::illegalVarargUsage);
	}

	private String compileToNumber(RValue value) throws CompilationFailure {
		return "StandardLibrary.strictToNumber(" + value.accept(this) + ")";
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
		return "DynamicOps." + function + "(" + context + ", " + a.accept(this) + ", " + b.accept(this) + ")";
	}

	private String compileUnaryOperatorInvocation(LuaOperator op, RValue value) throws CompilationFailure {
		if (JavaOperators.canApplyJavaSymbol(op, null, value.typeInfo())) {
			String javaOp = Objects.requireNonNull(JavaOperators.javaSymbol(op));
			return javaOp + " " + value.accept(this);
		}
		// if there is no corresponding Java operator, call the runtime API
		String function = op.name().toLowerCase();
		var context = nestedData.contextName();
		return "DynamicOps." + function + "(" + context + ", " + value.accept(this) + ")";
	}

	private String compileTableWrite(RValue table, RValue key, RValue value) throws CompilationFailure {
		return "TableOps.setIndex(" + table.accept(this) + ", " + key.accept(this) + ", " + value.accept(this) + ")";
	}

	private String compileTableRead(RValue table, RValue key) throws CompilationFailure {
		return "TableOps.index(" + table.accept(this) + ", " + key.accept(this) + ")";
	}

	private String compileFunctionCall(RValue function, List<RValue> arguments) throws CompilationFailure {
		var argList = new ArrayList<>(arguments);
		boolean isVararg = !argList.isEmpty() && argList.get(argList.size() - 1).isVararg();
		if (isVararg) {
			// put the last element in the first position
			argList.add(0, argList.remove(argList.size() - 1));
		}
		var args = commaList(argList);
		var context = nestedData.contextName();
		return "FunctionOps.call(" + function.accept(this) + ", " + context + (argList.isEmpty() ? "" : ", " + args) + ")";
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

	private CompilationFailure illegalVarargUsage() {
		var msg = Message.create("Cannot use ... outside of vararg function");
		msg.setLevel(Level.ERROR);
		statementVisitor.context.reporter().report(msg);
		return new CompilationFailure(Tag.BAD_INPUT);
	}
}