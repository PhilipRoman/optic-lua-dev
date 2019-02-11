package optic.lua.codegen.java;

import optic.lua.asm.*;
import optic.lua.asm.RValue.*;
import optic.lua.asm.instructions.VariableMode;
import optic.lua.codegen.ResultBuffer;
import optic.lua.messages.*;
import optic.lua.util.*;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

public class JavaExpressionVisitor implements RValueVisitor<String, CompilationFailure> {
	private final NestedData nestedData;
	private final JavaCodeOutput statementVisitor;

	private Options options() {
		return statementVisitor.context.options();
	}

	public JavaExpressionVisitor(NestedData data, JavaCodeOutput visitor) {
		nestedData = Objects.requireNonNull(data);
		statementVisitor = visitor;
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
				buffer.add("\tObject[] " + varargName + " = ListOps.sublist(" + argsName + ", " + offset + ");");
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
}
