package optic.lua.codegen.java;

import optic.lua.asm.*;
import optic.lua.codegen.ResultBuffer;
import optic.lua.messages.*;
import optic.lua.optimization.ProvenType;
import optic.lua.util.UniqueNames;
import org.jetbrains.annotations.NotNull;
import org.slf4j.*;

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
public class JavaCodeOutput implements StepVisitor<ResultBuffer, CompilationFailure> {
	public static final String INJECTED_CONTEXT_PARAM_NAME = "INJECTED_LUA_CONTEXT";
	public static final String INJECTED_ARGS_PARAM_NAME = "INJECTED_LUA_ARGS";
	private static final boolean USE_INJECTED_CONTEXT = true;
	private static final boolean USE_INJECTED_ARGS = true;
	private static final Logger log = LoggerFactory.getLogger(JavaCodeOutput.class);
	final Options options;
	private final List<String> constants = new ArrayList<>();
	private final NestedData nestedData = new NestedData();
	private final JavaExpressionVisitor expressionVisitor = new JavaExpressionVisitor(nestedData, this);

	public JavaCodeOutput(Options options) {
		this.options = Objects.requireNonNull(options);
	}

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
	public ResultBuffer visitForRangeLoop(VariableInfo counter, RValue from, RValue to, RValue step, AsmBlock block) throws CompilationFailure {
		var buffer = new ResultBuffer();
		var realCounterName = "i_" + counter.getName();
		ProvenType realCounterType = from.typeInfo().and(step.typeInfo());
		if (realCounterType == ProvenType.INTEGER
				&& options.get(StandardFlags.LOOP_SPLIT)) {
			// we optimize integer loops at runtime by checking if the range is within int bounds
			// that way the majority of loops can run with int as counter and the long loop is just a safety measure
			// it has been proven repeatedly that int loops are ~30% faster than long loops and 300% faster than float/double loops
			// int loop
			buffer.add("if(", expression(from), " >= Integer.MIN_VALUE && ", expression(to), " <= Integer.MAX_VALUE && (long) ", expression(step), " == ", expression(step), ")");
			buffer.add("for(int ", realCounterName, " = (int)", expression(from), "; ", realCounterName, " <= (int)", expression(to), "; ", realCounterName, " += ", expression(step), ") {");
			buffer.add("long ", counter.getName(), " = ", realCounterName, ";");
			buffer.addBlock(visitAll(block.steps()));
			buffer.add("}");
			buffer.add("else");
		}
		// regular for-loop
		buffer.add("for(", JavaUtils.typeName(realCounterType), " ", realCounterName, " = ", expression(from), "; ", realCounterName, " <= ", expression(to), "; ", realCounterName, " += ", expression(step), ") {");
		buffer.add(JavaUtils.typeName(counter), " ", counter.getName(), " = ", realCounterName, ";");
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
			boolean debug = options.get(StandardFlags.DEBUG_COMMENTS);
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
		var buffer = new ResultBuffer();
		var iteratorName = "iterator_" + UniqueNames.next();
		var eachName = "each_" + UniqueNames.next();
		buffer.add("Iterator ", iteratorName, " = (Iterator) ", expression(iterator), ";");
		buffer.add("while(", iteratorName, ".hasNext()) {");
		buffer.add("Object[] ", eachName, " = (Object[]) ", iteratorName, ".next();");
		int i = 0;
		for (var variable : variables) {
			buffer.add(JavaUtils.typeName(variable), " ", variable.getName(), " = ListOps.get(", eachName, ", ", i++, ");");
		}
		buffer.addBlock(visitAll(body.steps()));
		buffer.add("}");
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
		var buffer = new ResultBuffer();
		buffer.add("while(true) {");
		buffer.addBlock(visitAll(body.steps()));
		buffer.add("}");
		return buffer;
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
	public ResultBuffer visitBreakIf(RValue condition, boolean isTrue) throws CompilationFailure {
		var buffer = new ResultBuffer();
		buffer.add("if(", (isTrue ? "" : "!"), "DynamicOps.isTrue(", expression(condition), ")) break;");
		return buffer;
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
		if (options.get(StandardFlags.KEEP_COMMENTS)) {
			buffer.add("// ", comment);
		}
		return buffer;
	}

	@Override
	public ResultBuffer visitAssignment(Register register, RValue value) throws CompilationFailure {
		var buffer = new ResultBuffer();
		buffer.add(JavaUtils.typeName(register), " ", register.name(), " = ", expression(value), ";");
		return buffer;
	}

	public String generate(AsmBlock block) throws CompilationFailure {
		var buffer = new ResultBuffer();
		if (options.get(StandardFlags.ALLOW_UPVALUE_VARARGS)) {
			log.warn("Use of {} is not officially supported", StandardFlags.ALLOW_UPVALUE_VARARGS);
		}
		var result = new ByteArrayOutputStream(4096);
		var out = new PrintStream(result);

		out.println("import optic.lua.runtime.*;");
		out.println("import optic.lua.runtime.invoke.*;");
		out.println("import java.util.Iterator;");
		var contextName = nestedData.pushNewContextName();
		out.println("static Object[] main(final LuaContext " + contextName + ", Object[] args) { if(1 == 1) {");

		buffer.addBlock(visitAll(block.steps()));
		buffer.add("} return ListOps.empty(); }");
		String context = USE_INJECTED_CONTEXT ? INJECTED_CONTEXT_PARAM_NAME : "LuaContext.create()";
		String args = USE_INJECTED_ARGS ? INJECTED_ARGS_PARAM_NAME : "new Object[0]";
		buffer.add("return main(", context, ", ", args, ");");

		constants.forEach(out::println);
		buffer.writeTo(out, options.get(Option.INDENT));
		out.flush();
		return result.toString();
	}

	void addConstant(String type, String name, String value) {
		constants.add("final " + type + " " + name + " = " + value + ";");
	}

	@Override
	public String toString() {
		return getClass().getName();
	}
}
