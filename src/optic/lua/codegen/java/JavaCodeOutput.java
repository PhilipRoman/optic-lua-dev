package optic.lua.codegen.java;

import optic.lua.CompilerPlugin;
import optic.lua.asm.*;
import optic.lua.asm.instructions.Void;
import optic.lua.asm.instructions.*;
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
public class JavaCodeOutput extends StepVisitor<ResultBuffer> implements CompilerPlugin {
	private final PrintStream out;
	private final AsmBlock block;
	final Context context;
	public static final String INJECTED_CONTEXT_PARAM_NAME = "INJECTED_LUA_CONTEXT";
	private static final boolean USE_INJECTED_CONTEXT = true;
	public static final String INJECTED_ARGS_PARAM_NAME = "INJECTED_LUA_ARGS";
	private static final boolean USE_INJECTED_ARGS = true;
	private final NestedData nestedData = new NestedData();
	private final JavaExpressionVisitor expressionVisitor = new JavaExpressionVisitor(nestedData, this);

	public ResultBuffer visitToNumber(@NotNull ToNumber toNumber) throws CompilationFailure {
		var buffer = new ResultBuffer();
		var from = toNumber.getSource();
		var to = toNumber.getTarget();
		buffer.add("double ", to.getName(), " = StandardLibrary.toNumber(", expression(from), ");");
		return buffer;
	}

	public ResultBuffer visitReturn(@NotNull Return ret) throws CompilationFailure {
		var buffer = new ResultBuffer();
		var values = ret.getValues();
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

	public ResultBuffer visitGetVarargs(@NotNull GetVarargs getVarargs) throws CompilationFailure {
		var buffer = new ResultBuffer();
		var varargsName = nestedData.varargName();
		if (varargsName.isPresent()) {
			var varargs = varargsName.get();
			buffer.add("Object[] ", getVarargs.getTo().getName(), " = ", varargs, ";");
		} else {
			illegalVarargUsage();
		}
		return buffer;
	}

	public ResultBuffer visitSelect(@NotNull Select select) throws CompilationFailure {
		var buffer = new ResultBuffer();
		var n = select.getN();
		var vararg = expression(select.getVarargs());
		var target = select.getOut().getName();
		buffer.add("Object ", target, " = ListOps.get(", vararg, ", ", n, ");");
		return buffer;
	}

	public ResultBuffer visitWrite(@NotNull Write write) throws CompilationFailure {
		var buffer = new ResultBuffer();
		switch (write.getTarget().getMode()) {
			case UPVALUE: {
				if (write.getTarget().isEnv()) {
					var context = nestedData.contextName();
					buffer.add(context, "._ENV = ", expression(write.getSource()), ";");
					return buffer;
				} else if (!write.getTarget().isFinal()) {
					buffer.add(write.getTarget(), ".set(", expression(write.getSource()), ");");
					return buffer;
				}
				// if upvalue is final, fall through to LOCAL branch
			}
			case LOCAL: {
				if (write.getTarget().typeInfo().isNumeric() && !write.getSource().typeInfo().isNumeric())
					buffer.add(write.getTarget(), " = StandardLibrary.toNumber(", expression(write.getSource()), ");");
				else
					buffer.add(write.getTarget(), " = ", expression(write.getSource()), ";");
				return buffer;
			}
			case GLOBAL: {
				var context = nestedData.contextName();
				buffer.add(context, ".setGlobal(\"", write.getTarget().getName(), "\", ", expression(write.getSource()), ");");
				return buffer;
			}
			default:
				throw new AssertionError();
		}
	}

	public ResultBuffer visitForRangeLoop(@NotNull ForRangeLoop loop) throws CompilationFailure {
		var buffer = new ResultBuffer();
		var from = expression(loop.getFrom());
		var to = expression(loop.getTo());
		var counter = loop.getCounter();
		var counterName = "i_" + counter.getName();
		String counterType = JavaUtils.typeName(loop.getFrom().typeInfo());
		String counterTypeName = JavaUtils.typeName(loop.getCounter());
		if (counterTypeName.equals("long") && context.options().get(StandardFlags.LOOP_SPLIT)) {
			// we optimize integer loops at runtime by checking if the range is within int bounds
			// that way the majority of loops can run with int as counter and the long loop is just a safety measure
			// it has been proven repeatedly that int loops are ~30% faster than long loops and 300% faster than float/double loops
			// int loop
			buffer.add("if(", from, " >= Integer.MIN_VALUE && ", to, " <= Integer.MAX_VALUE)");
			buffer.add("for(int ", counterName, " = (int)", from, "; ", counterName, " <= (int)", to, "; ", counterName, "++) {");
			buffer.add("long ", counter.getName(), " = ", counterName, ";");
			buffer.addBlock(visitAll(loop.getBlock().steps()));
			buffer.add("}");
			buffer.add("else");
		}
		// regular for-loop
		buffer.add("for(", counterType, " ", counterName, " = ", from, "; ", counterName, " <= ", to, "; ", counterName, "++) {");
		buffer.add(counterTypeName, " ", counter.getName(), " = ", counterName, ";");
		buffer.addBlock(visitAll(loop.getBlock().steps()));
		buffer.add("}");
		return buffer;
	}

	@Override
	public ResultBuffer visitDeclare(@NotNull Declare declare) {
		var buffer = new ResultBuffer();
		VariableInfo variable = declare.getVariable();
		String name = declare.getName();
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

	public ResultBuffer visitComment(@NotNull Comment comment) {
		var buffer = new ResultBuffer();
		if (context.options().get(StandardFlags.KEEP_COMMENTS)) {
			buffer.add("// ", comment.getText());
		}
		return buffer;
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

	public ResultBuffer visitIfElseChain(@NotNull IfElseChain ifElseChain) throws CompilationFailure {
		var buffer = new ResultBuffer();
		int size = ifElseChain.getClauses().size();
		int i = 0;
		for (var entry : ifElseChain.getClauses().entrySet()) {
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

	public ResultBuffer visitBlock(@NotNull Block block) throws CompilationFailure {
		var buffer = new ResultBuffer();
		buffer.add("{");
		buffer.addBlock(visitAll(block.getSteps().steps()));
		buffer.add("}");
		return buffer;
	}

	@Override
	public ResultBuffer visitVoid(@NotNull Void x) throws CompilationFailure {
		var buffer = new ResultBuffer();
		buffer.add(expression(x.getInvocation()) + ";");
		return buffer;
	}

	@Override
	public ResultBuffer visitAssign(@NotNull Assign x) throws CompilationFailure {
		var buffer = new ResultBuffer();
		buffer.add(JavaUtils.typeName(x.getResult()), " ", x.getResult().getName(), " = ", expression(x.getValue()), ";");
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
		buffer.add("main(", context, ", ", args, ");");
		buffer.writeTo(out, this.context.options().get(Option.INDENT));
		out.flush();
	}

	private void illegalVarargUsage() throws CompilationFailure {
		var msg = Message.create("Cannot use ... outside of vararg function");
		msg.setLevel(Level.ERROR);
		context.reporter().report(msg);
		throw new CompilationFailure();
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

	@Override
	public ResultBuffer defaultValue(@NotNull Step x) {
		throw new IllegalArgumentException("No handler for " + x.getClass());
	}
}
