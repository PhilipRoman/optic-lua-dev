package optic.lua.codegen.java;

import optic.lua.asm.*;
import optic.lua.asm.instructions.*;
import optic.lua.codegen.*;
import optic.lua.messages.*;
import optic.lua.util.UniqueNames;

import java.io.*;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

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
public class JavaCodeOutput {
	private final TemplateOutput out;
	private final AsmBlock block;
	private final MessageReporter reporter;
	private boolean keepComments = true;
	/**
	 * <p>
	 * Stack containing names of varargs variables in nested functions.
	 * There is an entry for each level of nested functions. If an Optional
	 * is empty, it means that function did not have a vararg parameter.
	 * </p>
	 * <p>
	 * Initially, the stack contains the root vararg.
	 * </p>
	 */
	private final Deque<Optional<String>> varargNamesInFunction = new ArrayDeque<>(List.of(
			Optional.of("vararg" + UniqueNames.next()))
	);

	private static final Map<Class<? extends Step>, WriterFunction> TABLE;

	static {
		Map<Class<? extends Step>, WriterFunction> table = new HashMap<>(20);
		table.put(Block.class, JavaCodeOutput::writeBlock);
		table.put(Branch.class, JavaCodeOutput::writeBranch);
		table.put(Call.class, JavaCodeOutput::writeCall);
		table.put(Comment.class, JavaCodeOutput::writeComment);
		table.put(Declare.class, JavaCodeOutput::writeDeclare);
		table.put(ForRangeLoop.class, JavaCodeOutput::writeForRangeLoop);
		table.put(FunctionLiteral.class, JavaCodeOutput::writeFunctionLiteral);
		table.put(GetVarargs.class, JavaCodeOutput::writeGetVarargs);
		table.put(LoadConstant.class, JavaCodeOutput::writeLoadConstant);
		table.put(MakeTable.class, JavaCodeOutput::writeMakeTable);
		table.put(Operator.class, JavaCodeOutput::writeOperator);
		table.put(Read.class, JavaCodeOutput::writeRead);
		table.put(Return.class, JavaCodeOutput::writeReturn);
		table.put(Select.class, JavaCodeOutput::writeSelect);
		table.put(TableRead.class, JavaCodeOutput::writeTableRead);
		table.put(TableWrite.class, JavaCodeOutput::writeTableWrite);
		table.put(Write.class, JavaCodeOutput::writeWrite);
		table.put(ToNumber.class, JavaCodeOutput::writeToNumber);
		TABLE = Map.copyOf(table);
	}

	private void writeToNumber(Step step) {
		var toNumber = (ToNumber) step;
		var from = toNumber.getSource();
		var to = toNumber.getTarget();
		out.printLine("double ", to, " = StandardLibrary.toNumber(", from, ");");
	}

	private void writeFunctionLiteral(Step step) throws CompilationFailure {
		var fn = (FunctionLiteral) step;
		var target = fn.getAssignTo().getName();
		var params = fn.getParams().list();
		var argsName = "args" + UniqueNames.next();
		out.printLine("LuaFunction ", target, " = new LuaFunction(){Object[] call(Object[] ", argsName, ") {");
		out.addIndent();
		for (var p : params) {
			if (p.equals("...")) {
				var varargName = "vararg" + UniqueNames.next();
				varargNamesInFunction.addLast(Optional.of(varargName));
				int offset = params.size() - 1;
				out.printLine("Object[] ", varargName, " = ListOps.sublist(", argsName, ", ", offset, ");");
			} else {
				out.printLine("Object ", p, " = ListOps.get(", argsName, ", ", params.indexOf(p), ");");
			}
		}
		if (!fn.getParams().hasVarargs()) {
			varargNamesInFunction.addLast(Optional.empty());
		}
		out.printLine("if(1 == 1) {");
		for (var s : fn.getBody().steps()) {
			write(s);
		}
		out.printLine("}");
		out.printLine("return ListOps.empty();");
		varargNamesInFunction.removeLast();
		out.removeIndent();
		out.printLine("}};");
	}

	private void writeLoadConstant(Step step) {
		var c = ((LoadConstant) step).getConstant();
		var target = ((LoadConstant) step).getTarget().getName();
		if (c == null) {
			c = "null";
		} else if (c instanceof String) {
			c = '"' + c.toString() + '"';
		}
		String typeName = c.getClass().getSimpleName();
		if (c.getClass() == Double.class) {
			typeName = "double";
		}
		out.printLine(typeName, " ", target, " = ", c, ";");
	}

	private void writeMakeTable(Step step) {
		var mt = (MakeTable) step;
		var result = mt.getResult();
		var map = new HashMap<>(mt.getValues());
		Optional<Entry<Register, Register>> vararg = map.entrySet().stream()
				.filter(e -> e.getValue().isVararg())
				.findAny();
		// vararg is treated separately, remove it from the map
		vararg.ifPresent(v -> map.remove(v.getKey()));
		String list = map.entrySet().stream().map(e -> e.getKey() + ", " + e.getValue()).collect(Collectors.joining(", "));
		vararg.ifPresentOrElse(o -> {
			var offset = o.getKey();
			var value = o.getValue().getName();
			out.printLine("LuaTable ", result, " = TableOps.createWithVararg(", offset, ", ", value, ", ", list, ");");
		}, () -> {
			out.printLine("LuaTable ", result, " = TableOps.create(", list, ");");
		});
	}

	private void writeOperator(Step step) {
		var op = (Operator) step;
		var target = op.getTarget().getName();
		var a = Optional.ofNullable(op.getA()).map(Register::getName).orElse("");
		var b = op.getB().getName();
		if (Set.of("*", "+", "-", "/").contains(op.getSymbol())
				&& op.getA() != null
				&& op.getA().status() == TypeStatus.NUMBER
				&& op.getB().status() == TypeStatus.NUMBER
				&& op.getTarget().status() == TypeStatus.NUMBER) {
			writeComment("Fast numeric operation between " + op.getA().toDebugString() + " and " + op.getB().toDebugString());
			out.printLine("double ", target, " = ", a, " ", op.getSymbol(), " ", b, ";");
			return;
		}
		var symbolNames = Map.of(
				"+", "add",
				"-", "sub",
				"*", "mul",
				"/", "div"
		);
		if (op.getA() != null && symbolNames.containsKey(op.getSymbol())) {
			String resultType = (op.getA().status() == TypeStatus.NUMBER) ? "double" : "Object";
			String function = symbolNames.get(op.getSymbol());
			out.printLine(resultType, " ", target, " = DynamicOps.", function, "(", a, ", ", b, ");");
		} else {
			var symbolString = '"' + op.getSymbol() + '"';
			out.printLine("Object ", target, " = DynamicOps.operator(", a, ", ", symbolString, ", ", b, ");");
		}
	}

	private void writeRead(Step step) {
		var read = (Read) step;
		writeComment("read " + read.getSourceInfo().toDebugString() + " to " + read.getRegister().toDebugString());
		switch (read.getSourceInfo().getMode()) {
			case LOCAL: {
				if (read.getSourceInfo().status() == TypeStatus.NUMBER && read.getRegister().status() == TypeStatus.NUMBER) {
					out.printLine("double ", read.getRegister(), " = ", read.getName(), ";");
				} else {
					out.printLine("Object ", read.getRegister(), " = ", read.getName(), ";");
				}
				break;
			}
			case UPVALUE: {
				out.printLine("Object ", read.getRegister(), " = ", read.getName(), ".get();");
				break;
			}
			case GLOBAL: {
				out.printLine("Object ", read.getRegister(), " = EnvOps.get(_ENV, \"", read.getName(), "\");");
				break;
			}
			default:
				throw new AssertionError();
		}
	}

	private void writeReturn(Step step) {
		var ret = (Return) step;
		var regs = ret.getRegisters();
		if (!regs.isEmpty() && regs.get(regs.size() - 1).isVararg()) {
			var varargs = regs.get(regs.size() - 1).getName();
			var values = new ArrayList<>(regs);
			values.remove(values.size() - 1);
			out.printLine("return ListOps.concat(", varargs, ", ", commaList(values), ");");
		} else {
			out.printLine("return ListOps.create(", commaList(regs), ");");
		}
	}

	private void writeGetVarargs(Step step) throws CompilationFailure {
		var g = (GetVarargs) step;
		var varargsName = varargNamesInFunction.getLast();
		if (varargsName.isPresent()) {
			var varargs = varargsName.get();
			out.printLine("Object[] ", g.getTo().getName(), " = ", varargs, ";");
		} else {
			illegalVarargUsage();
		}
	}

	private void writeSelect(Step step) {
		var select = (Select) step;
		var n = select.getN();
		var vararg = select.getVarargs().getName();
		var target = select.getOut().getName();
		out.printLine("Object ", target, " = ListOps.get(", vararg, ", ", n, ");");
	}

	private void writeTableRead(Step step) {
		var read = (TableRead) step;
		out.printLine("Object ", read.getOut(), " = DynamicOps.index(", read.getTable(), ", ", read.getKey(), ");");
	}

	private void writeTableWrite(Step step) {
		var write = (TableWrite) step;
		out.printLine("DynamicOps.setIndex(", write.getField().getTable(), ", ", write.getField().getKey(), ", ", write.getValue(), ");");
	}

	private void writeWrite(Step step) {
		var write = (Write) step;
		switch (write.getTarget().getMode()) {
			case LOCAL: {
				if (write.getTarget().status() == TypeStatus.NUMBER && write.getSource().status() != TypeStatus.NUMBER)
					out.printLine(write.getTarget(), " = StandardLibrary.toNumber(", write.getSource().getName(), ");");
				else
					out.printLine(write.getTarget(), " = ", write.getSource().getName(), ";");
				break;
			}
			case UPVALUE: {
				out.printLine(write.getTarget(), ".set(", write.getSource().getName(), ");");
				break;
			}
			case GLOBAL: {
				out.printLine("EnvOps.set(_ENV, \"", write.getTarget(), "\", ", write.getSource().getName(), ");");
				break;
			}
			default:
				throw new AssertionError();
		}
	}

	private void writeForRangeLoop(Step step) throws CompilationFailure {
		var loop = ((ForRangeLoop) step);
		var from = loop.getFrom().getName();
		var to = loop.getTo().getName();
		var counter = loop.getCounter();
		var counterName = "i_" + counter.getName();
		out.printLine("for(double ", counterName, " = ", from, "; ", counterName, " <= ", to, "; ", counterName, "++) {");
		out.addIndent();
		String counterTypeName = loop.getCounter().status() == TypeStatus.NUMBER ? "double" : "Object";
		out.printLine(counterTypeName, " ", counter.getName(), " = ", counterName, ";");
		for (Step s : loop.getBlock().steps()) {
			write(s);
		}
		out.removeIndent();
		out.printLine("}");
	}

	private void writeDeclare(Step step) {
		var declare = (Declare) step;
		assert declare.getVariable().getMode() != VariableMode.GLOBAL;
		if (declare.getVariable().getMode() == VariableMode.LOCAL) {
			String finalPrefix = declare.getVariable().isFinal() ? "final " : "";
			if (declare.getVariable().status() == TypeStatus.NUMBER) {
				out.printLine(finalPrefix, "double ", ((Declare) step).getName(), ";");
			} else {
				out.printLine(finalPrefix, "Object ", ((Declare) step).getName(), ";");
			}
		} else {
			out.printLine("final UpValue ", declare.getName(), " = UpValue.create();");
		}
	}

	private void writeComment(Step step) {
		if (keepComments) {
			out.printLine("// ", ((Comment) step).getText());
		}
	}

	private void writeComment(String comment) {
		if (keepComments) {
			out.printLine("// ", comment);
		}
	}

	private void writeCall(Step step) {
		var call = (Call) step;
		var function = call.getFunction().getName();
		var argList = new ArrayList<>(call.getArgs());
		boolean isVararg = !argList.isEmpty() && argList.get(argList.size() - 1).isVararg();
		if (isVararg) {
			// put the last element in the first position
			argList.add(0, argList.remove(argList.size() - 1));
		}
		var prefix = call.getOutput().isUnused() ? "" : ("Object[] " + call.getOutput().getName() + " = ");
		var args = commaList(argList);
		out.printLine(prefix, "FunctionOps.call(", function, argList.isEmpty() ? "" : ", ", args, ");");
	}

	private static CharSequence commaList(List<Register> args) {
		int size = args.size();
		var builder = new StringBuilder(size * 5 + 10);
		for (int i = 0; i < size; i++) {
			builder.append(args.get(i).getName());
			if (i != size - 1) {
				builder.append(", ");
			}
		}
		return builder;
	}

	private static String toNumber(VariableInfo info) {
		return info.status() == TypeStatus.NUMBER ? info.getName() : "StandardLibrary.toNumber(" + info.getName() + ")";
	}

	private static String toNumber(Register r) {
		return r.status() == TypeStatus.NUMBER ? r.getName() : "StandardLibrary.toNumber(" + r.getName() + ")";
	}

	private void writeBranch(Step step) throws CompilationFailure {
		var branch = (Branch) step;
		out.printLine("if(DynamicOps.isTrue(", branch.getCondition().getName(), ")) {");
		out.addIndent();
		for (Step s : branch.getBody().steps()) {
			write(s);
		}
		out.removeIndent();
		out.printLine("}");
	}

	private void writeBlock(Step step) throws CompilationFailure {
		var block = (Block) step;
		out.printLine("{");
		out.addIndent();
		for (Step s : block.getSteps().steps()) {
			write(s);
		}
		out.removeIndent();
		out.printLine("}");
	}

	private void write(Step step) throws CompilationFailure {
		assert Modifier.isFinal(step.getClass().getModifiers());
		TABLE.get(step.getClass()).write(this, step);
	}

	private JavaCodeOutput(PrintStream out, AsmBlock block, MessageReporter reporter) {
		this.out = new TemplateOutput(out);
		this.block = block;
		this.reporter = reporter;
	}

	public static CodeOutput writingTo(OutputStream stream) {
		PrintStream printStream = stream instanceof PrintStream
				? (PrintStream) stream
				: new PrintStream(stream);
		return (steps, reporter) -> new JavaCodeOutput(printStream, steps, reporter).execute();
	}

	private void execute() throws CompilationFailure {
		var msg = Message.create("Java code output still in development!");
		msg.setLevel(Level.WARNING);
		reporter.report(msg);
		out.printLine("import optic.lua.runtime.*;");
		out.printLine("static Object[] main(final UpValue _ENV, Object[] args) { if(1 == 1) {");
		out.addIndent();
		for (var step : block.steps()) {
			write(step);
		}
		out.removeIndent();
		out.printLine("} return ListOps.empty(); }");
		out.printLine("main(UpValue.create(EnvOps.createEnv()), new Object[0]);");
	}

	private void illegalVarargUsage() throws CompilationFailure {
		var msg = Message.create("Cannot use ... outside of vararg function");
		msg.setLevel(Level.ERROR);
		reporter.report(msg);
		throw new CompilationFailure();
	}

	private interface WriterFunction {
		void write(JavaCodeOutput self, Step step) throws CompilationFailure;
	}
}
