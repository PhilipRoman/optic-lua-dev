package optic.lua.codegen.java;

import optic.lua.CompilerPlugin;
import optic.lua.asm.*;
import optic.lua.asm.VariableMode;
import optic.lua.codegen.ResultBuffer;
import optic.lua.messages.*;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.*;

/**
 * Creates Java source code from given {@link AsmBlock}.
 */
/*
 * A valid Java method (excluding void) must end with a return statement in
 * every possible code path. Lua does not have such a restriction and simply
 * returns [nil, nil, ...] when it reaches the end of a function. Furthermore,
 * in Java, unreachable code is illegal.
 *
 * Since verifying whether code is reachable would be complicated, I've
 * wrapped ALL function bodies like this:
 *
 * if(1 == 1) {
 *   functionBodyGoesHere();
 * }
 * return EMPTY_ARRAY;
 *
 * */
public class JavaCodeOutput implements StepVisitor<ResultBuffer, CompilationFailure>, CompilerPlugin {
	private final PrintStream out;
	private final AsmBlock block;
	final Context context;
	public static final String INJECTED_CONTEXT_PARAM_NAME = "INJECTED_LUA_CONTEXT";
	private static final boolean USE_INJECTED_CONTEXT = true;
	public static final String INJECTED_ARGS_PARAM_NAME = "INJECTED_LUA_ARGS";
	private static final boolean USE_INJECTED_ARGS = true;
	private final NestedData nestedData = new NestedData();
	private final JavaExpressionVisitor expressionVisitor = new JavaExpressionVisitor(nestedData, this);

	@Override
	public ResultBuffer visitReturn(List<RValue> values) throws CompilationFailure {
		var buffer = new ResultBuffer();
		if (!values.isEmpty() && values.get(values.size() - 1).isVararg()) {
			var varargs = values.get(values.size() - 1);
			var valuesCopy = new ArrayList<>(values);
			valuesCopy.remove(valuesCopy.size() - 1);
			buffer.add("return ListOps.concat(", expression(varargs), valuesCopy.isEmpty() ? "" : ", ", commaList(valuesCopy), ");");
		} else {
			buffer.add("return ListOps.create(", commaList(values), ");");
		}
		return buffer;
	}

	@Override
	public ResultBuffer visitGetVarargs(Register register) throws CompilationFailure {
		var buffer = new ResultBuffer();
		var varargsName = nestedData.varargName();
		if (varargsName.isPresent()) {
			var varargs = varargsName.get();
			buffer.add("Object[] ", register.getName(), " = ", varargs, ";");
		} else {
			illegalVarargUsage();
		}
		return buffer;
	}

	@Override
	public ResultBuffer visitSelect(Register target, int n, RValue vararg) throws CompilationFailure {
		var buffer = new ResultBuffer();
		buffer.add("Object ", target, " = ListOps.get(", expression(vararg), ", ", n, ");");
		return buffer;
	}

	@Override
	public ResultBuffer visitWrite(VariableInfo variable, RValue value) throws CompilationFailure {
		var buffer = new ResultBuffer();
		switch (variable.getMode()) {
			case UPVALUE: {
				if (variable.isEnv()) {
					var context = nestedData.contextName();
					buffer.add(context, "._ENV = ", expression(value), ";");
					return buffer;
				} else if (!variable.isFinal()) {
					buffer.add(variable.getName(), ".set(", expression(value), ");");
					return buffer;
				}
				// if upvalue is final, fall through to LOCAL branch
			}
			case LOCAL: {
				if (variable.typeInfo().isNumeric() && !value.typeInfo().isNumeric())
					buffer.add(variable, " = StandardLibrary.toNumber(", expression(value), ");");
				else
					buffer.add(variable, " = ", expression(value), ";");
				return buffer;
			}
			case GLOBAL: {
				var context = nestedData.contextName();
				buffer.add(context, ".setGlobal(\"", variable.getName(), "\", ", expression(value), ");");
				return buffer;
			}
			default:
				throw new AssertionError();
		}
	}

	@Override
	public ResultBuffer visitForRangeLoop(VariableInfo counter, RValue from, RValue to, AsmBlock block) throws CompilationFailure {
		var buffer = new ResultBuffer();
		var counterName = "i_" + counter.getName();
		String counterType = JavaUtils.typeName(from.typeInfo());
		String counterTypeName = JavaUtils.typeName(counter);
		if (counterTypeName.equals("long") && context.options().get(StandardFlags.LOOP_SPLIT)) {
			// we optimize integer loops at runtime by checking if the range is within int bounds
			// that way the majority of loops can run with int as counter and the long loop is just a safety measure
			// it has been proven repeatedly that int loops are ~30% faster than long loops and 300% faster than float/double loops
			// int loop
			buffer.add("if(", expression(from), " >= Integer.MIN_VALUE && ", to, " <= Integer.MAX_VALUE)");
			buffer.add("for(int ", counterName, " = (int)", expression(from), "; ", counterName, " <= (int)", expression(to), "; ", counterName, "++) {");
			buffer.add("long ", counter.getName(), " = ", counterName, ";");
			buffer.addBlock(visitAll(block.steps()));
			buffer.add("}");
			buffer.add("else");
		}
		// regular for-loop
		buffer.add("for(", counterType, " ", counterName, " = ", expression(from), "; ", counterName, " <= ", expression(to), "; ", counterName, "++) {");
		buffer.add(counterTypeName, " ", counter.getName(), " = ", counterName, ";");
		buffer.addBlock(visitAll(block.steps()));
		buffer.add("}");
		return buffer;
	}

	@Override
	public ResultBuffer visitDeclaration(VariableInfo variable) {
		var buffer = new ResultBuffer();
		String name = variable.getName();
		assert variable.getMode() != VariableMode.GLOBAL;
		if (variable.getMode() == VariableMode.LOCAL) {
			// local variable
			String finalPrefix = variable.isFinal() ? "final " : "";
			boolean debug = context.options().get(StandardFlags.DEBUG_COMMENTS);
			String debugSuffix = debug ? (" /* " + variable.toDebugString() + " */") : "";
			buffer.add(finalPrefix, JavaUtils.typeName(variable), " ", name, ";", debugSuffix);
		} else if (variable.isFinal()) {
			// final upvalue
			buffer.add("final ", JavaUtils.typeName(variable), " ", name, ";");
		} else {
			// upvalue
			buffer.add("final UpValue ", name, " = UpValue.create();");
		}
		return buffer;
	}

	@Override
	public ResultBuffer visitForEachLoop(List<VariableInfo> variables, RValue iterator, AsmBlock body) throws CompilationFailure {
		context.reporter().report(Message.createError("ForEachLoop currently not supported!"));
		throw new CompilationFailure(Tag.UNSUPPORTED_FEATURE);
	}

	private CharSequence commaList(List<RValue> args) throws CompilationFailure {
		int size = args.size();
		var builder = new StringBuilder(size * 5 + 10);
		for (int i = 0; i < size; i++) {
			builder.append(expression(args.get(i)));
			if (i != size - 1) {
				builder.append(", ");
			}
		}
		return builder;
	}

	@NotNull
	private CharSequence expression(RValue expression) throws CompilationFailure {
		return Objects.requireNonNull(expression.accept(expressionVisitor));
	}

	@Override
	public ResultBuffer visitIfElseChain(LinkedHashMap<FlatExpr, AsmBlock> clauses) throws CompilationFailure {
		var buffer = new ResultBuffer();
		int size = clauses.size();
		int i = 0;
		for (var entry : clauses.entrySet()) {
			boolean isLast = ++i == size;
			FlatExpr condition = entry.getKey();
			AsmBlock value = entry.getValue();
			buffer.addBlock(visitAll(condition.block()));
			buffer.add("if(DynamicOps.isTrue(", expression(condition.value()), ")) {");
			buffer.addBlock(visitAll(value.steps()));
			buffer.add("}", isLast ? "" : " else {");
		}
		for (int j = 0; j < size - 1; j++) {
			buffer.add("}");
		}
//		buffer.add(String.join("", Collections.nCopies(size - 1, "}")));
		return buffer;
	}

	@Override
	public ResultBuffer visitLoop(AsmBlock body) throws CompilationFailure {
		context.reporter().report(Message.createError("Loop currently not supported!"));
		throw new CompilationFailure(Tag.UNSUPPORTED_FEATURE);
	}

	@Override
	public ResultBuffer visitBlock(AsmBlock block) throws CompilationFailure {
		var buffer = new ResultBuffer();
		buffer.add("{");
		buffer.addBlock(visitAll(block.steps()));
		buffer.add("}");
		return buffer;
	}

	@Override
	public ResultBuffer visitBreakIf(RValue condition) throws CompilationFailure {
		context.reporter().report(Message.createError("BreakIf currently not supported!"));
		throw new CompilationFailure(Tag.UNSUPPORTED_FEATURE);
	}

	@Override
	public ResultBuffer visitVoid(RValue.Invocation invocation) throws CompilationFailure {
		var buffer = new ResultBuffer();
		buffer.add(expression(invocation) + ";");
		return buffer;
	}

	@Override
	public ResultBuffer visitComment(String comment) {
		var buffer = new ResultBuffer();
		if (context.options().get(StandardFlags.KEEP_COMMENTS)) {
			buffer.add("// ", comment);
		}
		return buffer;
	}

	@Override
	public ResultBuffer visitAssignment(Register register, RValue value) throws CompilationFailure {
		var buffer = new ResultBuffer();
		buffer.add(JavaUtils.typeName(register), " ", register.getName(), " = ", expression(value), ";");
		return buffer;
	}

	private JavaCodeOutput(PrintStream out, AsmBlock block, Context context) {
		this.out = out;
		this.block = block;
		this.context = context;
	}

	public static CompilerPlugin.Factory writingTo(OutputStream stream) {
		PrintStream printStream = new PrintStream(new BufferedOutputStream(stream));
		return (steps, context) -> new JavaCodeOutput(printStream, steps, context);
	}


	private void execute() throws CompilationFailure {
		var buffer = new ResultBuffer();
		var msg = Message.create("Java code output still in development!");
		msg.setLevel(Level.WARNING);
		context.reporter().report(msg);
		buffer.add("import optic.lua.runtime.*;");
		var contextName = nestedData.pushNewContextName();
		buffer.add("static Object[] main(final LuaContext ", contextName, ", Object[] args) { if(1 == 1) {");
		buffer.addBlock(visitAll(block.steps()));
		buffer.add("} return ListOps.empty(); }");
		String context = USE_INJECTED_CONTEXT ? INJECTED_CONTEXT_PARAM_NAME : "LuaContext.create()";
		String args = USE_INJECTED_ARGS ? INJECTED_ARGS_PARAM_NAME : "new Object[0]";
		buffer.add("return main(", context, ", ", args, ");");
		buffer.writeTo(out, this.context.options().get(Option.INDENT));
		out.flush();
	}

	private void illegalVarargUsage() throws CompilationFailure {
		var msg = Message.create("Cannot use ... outside of vararg function");
		msg.setLevel(Level.ERROR);
		context.reporter().report(msg);
		throw new CompilationFailure(Tag.BAD_INPUT);
	}

	@Override
	public AsmBlock apply() throws CompilationFailure {
		execute();
		return block;
	}

	@Override
	public boolean concurrent() {
		return true;
	}

	@Override
	public String toString() {
		return getClass().getName();
	}
}
