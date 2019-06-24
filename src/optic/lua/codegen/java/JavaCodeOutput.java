package optic.lua.codegen.java;

import optic.lua.asm.*;
import optic.lua.asm.ListNode.Invocation;
import optic.lua.codegen.*;
import optic.lua.messages.*;
import optic.lua.optimization.StaticType;
import optic.lua.util.UniqueNames;
import org.jetbrains.annotations.NotNull;
import org.slf4j.*;

import java.io.*;
import java.util.*;

import static optic.lua.asm.InvocationMethod.*;
import static optic.lua.codegen.java.JavaExpressionVisitor.LOCAL_VARIABLE_PREFIX;

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
public final class JavaCodeOutput implements StatementVisitor<ResultBuffer, CompilationFailure> {
	private static final Logger log = LoggerFactory.getLogger(JavaCodeOutput.class);
	final Options options;
	private final List<String> constants = new ArrayList<>();
	private final NestedData nestedData = new NestedData();
	private final JavaExpressionVisitor expressionVisitor = new JavaExpressionVisitor(nestedData, this);
	// [J1, L1, J2, L2, ...] where J = Java line and L = Lua line
	// one-based indexing
	private final List<Integer> lineTable = new ArrayList<>();

	public JavaCodeOutput(Options options) {
		this.options = Objects.requireNonNull(options);
	}

	@Override
	public ResultBuffer visitReturn(ListNode values) throws CompilationFailure {
		var buffer = new LineList();
		buffer.addLine("return ", expression(values), ";");
		return buffer;
	}

	@Override
	public ResultBuffer visitWrite(VariableInfo variable, ExprNode value) throws CompilationFailure {
		var buffer = new LineList();
		switch (variable.getMode()) {
			case UPVALUE: {
				if (variable.isEnv()) {
					var context = nestedData.contextName();
					buffer.addLine(context, "._ENV = ", expression(value), ";");
					return buffer;
				} else if (!variable.isFinal()) {
					buffer.addLine(LOCAL_VARIABLE_PREFIX, variable.getName(), ".value = ", expression(value), ";");
					return buffer;
				}
				// if upvalue is final, fall through to LOCAL branch
			}
			case LOCAL: {
				if (variable.typeInfo().isNumeric() && !value.typeInfo().isNumeric())
					buffer.addLine(LOCAL_VARIABLE_PREFIX, variable, " = StandardLibrary.toNumber(", expression(value), ");");
				else
					buffer.addLine(LOCAL_VARIABLE_PREFIX, variable, " = ", expression(value), ";");
				return buffer;
			}
			case GLOBAL: {
				var context = nestedData.contextName();
				buffer.addLine(context, ".setGlobal(\"", variable.getName(), "\", ", expression(value), ");");
				return buffer;
			}
			default:
				throw new AssertionError();
		}
	}

	@Override
	public ResultBuffer visitLineNumber(int luaLine) {
		var buffer = new LineList();
		buffer.addChild(new LineNumberRecorder(lineTable, luaLine));
		buffer.addLine("// line ", luaLine);
		return buffer;
	}

	@Override
	public ResultBuffer visitForRangeLoop(VariableInfo counter, ExprNode from, ExprNode to, ExprNode step, AsmBlock block) throws CompilationFailure {
		var buffer = new LineList();
		var realCounterName = "i_" + counter.getName();
		StaticType realCounterType = from.typeInfo().and(step.typeInfo());
		if (realCounterType == StaticType.INTEGER
				&& options.get(StandardFlags.LOOP_SPLIT)) {
			// we optimize integer loops at runtime by checking if the range is within int bounds
			// that way the majority of loops can run with int as counter and the long loop is just a safety measure
			// it has been proven repeatedly that int loops are ~30% faster than long loops and 300% faster than float/double loops
			// int loop
			buffer.addLine("if(", expression(from), " >= Integer.MIN_VALUE && ", expression(to), " <= Integer.MAX_VALUE && (long) ", expression(step), " == ", expression(step), ")");
			buffer.addLine("for(int ", realCounterName, " = (int)", expression(from), "; ", realCounterName, " <= (int)", expression(to), "; ", realCounterName, " += ", expression(step), ") {");
			buffer.addLine("long ", LOCAL_VARIABLE_PREFIX, counter.getName(), " = ", realCounterName, ";");
			buffer.addAllChildren(visitAll(block.steps()));
			buffer.addLine("}");
			buffer.addLine("else");
		}
		// regular for-loop
		buffer.addLine("for(", JavaUtils.typeName(realCounterType), " ", realCounterName, " = ", expression(from), "; ", realCounterName, " <= ", expression(to), "; ", realCounterName, " += ", expression(step), ") {");
		buffer.addLine(JavaUtils.typeName(counter), " ", LOCAL_VARIABLE_PREFIX, counter.getName(), " = ", realCounterName, ";");
		buffer.addAllChildren(visitAll(block.steps()));
		buffer.addLine("}");
		return buffer;
	}

	@Override
	public ResultBuffer visitDeclaration(VariableInfo variable) {
		var buffer = new LineList();
		String name = variable.getName();
		assert variable.getMode() != VariableMode.GLOBAL;
		if (variable.getMode() == VariableMode.LOCAL) {
			// local variable
			String finalPrefix = variable.isFinal() ? "final " : "";
			buffer.addLine(finalPrefix, JavaUtils.typeName(variable), " ", LOCAL_VARIABLE_PREFIX, name, ";");
		} else if (variable.isFinal()) {
			// final upvalue
			buffer.addLine("final ", JavaUtils.typeName(variable), " ", LOCAL_VARIABLE_PREFIX, name, ";");
		} else {
			// upvalue
			buffer.addLine("final UpValue ", LOCAL_VARIABLE_PREFIX, name, " = new UpValue();");
		}
		return buffer;
	}

	@Override
	public ResultBuffer visitForEachLoop(List<VariableInfo> variables, ExprNode iterator, AsmBlock body) throws CompilationFailure {
		var buffer = new LineList();
		var iteratorName = "iterator_" + UniqueNames.next();
		var eachName = "each_" + UniqueNames.next();
		buffer.addLine("Iterator ", iteratorName, " = (Iterator) ", expression(iterator), ";");
		buffer.addLine("while(", iteratorName, ".hasNext()) {");
		buffer.addLine("Object[] ", eachName, " = (Object[]) ", iteratorName, ".next();");
		int i = 0;
		for (var variable : variables) {
			buffer.addLine(JavaUtils.typeName(variable), " ", LOCAL_VARIABLE_PREFIX, variable.getName(), " = get(", eachName, ", ", i++, ");");
		}
		buffer.addAllChildren(visitAll(body.steps()));
		buffer.addLine("}");
		return buffer;
	}

	@NotNull
	private ResultBuffer expression(ListNode expression) throws CompilationFailure {
		return Objects.requireNonNull(expression.accept(expressionVisitor));
	}

	@Override
	public ResultBuffer visitIfElseChain(LinkedHashMap<FlatExpr, AsmBlock> clauses) throws CompilationFailure {
		var buffer = new LineList();
		int size = clauses.size();
		int i = 0;
		for (var entry : clauses.entrySet()) {
			boolean isLast = ++i == size;
			FlatExpr condition = entry.getKey();
			AsmBlock value = entry.getValue();
			buffer.addAllChildren(visitAll(condition.block()));
			buffer.addLine("if(", expression(condition.value()), ") {");
			buffer.addAllChildren(visitAll(value.steps()));
			buffer.addLine("}", isLast ? "" : " else {");
		}
		for (int j = 0; j < size - 1; j++) {
			buffer.addLine("}");
		}
//		buffer.add(String.join("", Collections.nCopies(size - 1, "}")));
		return buffer;
	}

	@Override
	public ResultBuffer visitLoop(AsmBlock body) throws CompilationFailure {
		var buffer = new LineList();
		buffer.addLine("while(true) {");
		buffer.addAllChildren(visitAll(body.steps()));
		buffer.addLine("}");
		return buffer;
	}

	@Override
	public ResultBuffer visitBlock(AsmBlock block) throws CompilationFailure {
		var buffer = new LineList();
		buffer.addLine("{");
		buffer.addAllChildren(visitAll(block.steps()));
		buffer.addLine("}");
		return buffer;
	}

	@Override
	public ResultBuffer visitBreakIf(ExprNode condition, boolean isTrue) throws CompilationFailure {
		var buffer = new LineList();
		buffer.addLine("if(", (isTrue ? "" : "!"), "(", expression(condition), ")) break;");
		return buffer;
	}

	@Override
	public ResultBuffer visitVoid(ListNode value) throws CompilationFailure {
		var buffer = new LineList();
		var voidMethods = List.of(CALL, SET_INDEX);
		if (value instanceof Invocation && voidMethods.contains(((Invocation) value).getMethod())) {
			buffer.addLine(expression(value), ";");
		} else {
			buffer.addLine("use(", expression(value), ");");
		}
		return buffer;
	}

	@Override
	public ResultBuffer visitComment(String comment) {
		var buffer = new LineList();
		if (options.get(StandardFlags.KEEP_COMMENTS)) {
			buffer.addLine("// ", comment);
		}
		return buffer;
	}

	@Override
	public ResultBuffer visitAssignment(Register register, ExprNode value) throws CompilationFailure {
		var buffer = new LineList();
		buffer.addLine(JavaUtils.typeName(register), " ", register.name(), " = ", expression(value), ";");
		return buffer;
	}

	public String generate(String className, AsmBlock block) throws CompilationFailure {
		Objects.requireNonNull(className);
		Objects.requireNonNull(block);

		var buffer = new LineList();
		if (options.get(StandardFlags.ALLOW_UPVALUE_VARARGS)) {
			log.warn("Use of {} is not officially supported", StandardFlags.ALLOW_UPVALUE_VARARGS);
		}
		var result = new ByteArrayOutputStream(4096);
		var out = new PrintStream(result);

		buffer.addLine("import optic.lua.runtime.*;");
		buffer.addLine("import static optic.lua.runtime.DynamicOps.*;");
		buffer.addLine("import static optic.lua.runtime.ListOps.*;");
		buffer.addLine("import optic.lua.runtime.invoke.*;");
		buffer.addLine("import java.util.Iterator;");
		buffer.addLine("public class " + className + " {");
		var contextName = nestedData.pushNewContextName();

		LineList classBody = new LineList();
		classBody.addLine("public static Object[] run(final LuaContext " + contextName + ", Object[] args) { if(1 == 1) {");

		LineList methodBody = new LineList();
		methodBody.addAllChildren(visitAll(block.steps()));
		for (String constant : constants) {
			methodBody.prependString(constant);
		}
		classBody.addChild(methodBody);

		classBody.addLine("} return EMPTY; }");

		classBody.addLine("public static void main(String... args) {");
		classBody.addLine(new LineList("run(LuaContext.create(), args);"));
		classBody.addLine("}");

		buffer.addChild(classBody);

		// don't add the last closing brace yet
		buffer.writeTo(out, options.get(Option.INDENT));

		// append line table which only becomes populated after writing out the buffer
		var joiner = new StringJoiner(",");
		for (Integer lineNumber : lineTable) {
			joiner.add(lineNumber.toString());
		}
		out.println(options.get(Option.INDENT) + "public static final LineNumberTable LINE_TABLE = new LineNumberTable(" + joiner.toString() + ");");
		out.println("}");
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
