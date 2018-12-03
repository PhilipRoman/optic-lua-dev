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

public class JavaCodeOutput {
	private final TemplateOutput out;
	private final AsmBlock block;
	private final MessageReporter reporter;
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
		out.printLine("Dynamic ", to, " = DynamicOps.toNumber(", from, ");");
	}

	private void writeFunctionLiteral(Step step) throws CompilationFailure {
		var fn = (FunctionLiteral) step;
		var target = fn.getAssignTo().getName();
		var params = fn.getParams().list();
		var argsName = "args" + UniqueNames.next();
		out.printLine("Dynamic ", target, " = DynamicFunction.make((", argsName, ") -> {");
		out.addIndent();
		for (var p : params) {
			if (p.equals("...")) {
				var varargName = "vararg" + UniqueNames.next();
				varargNamesInFunction.addLast(Optional.of(varargName));
				out.printLine("MultiValue ", varargName, " = ", argsName, ".getVarargs();");
			} else {
				out.printLine("Dynamic ", p, " = ", argsName, ".get(", params.indexOf(p), ");");
			}
		}
		if (!fn.getParams().hasVarargs()) {
			varargNamesInFunction.addLast(Optional.empty());
		}
		for (var s : fn.getBody().steps()) {
			write(s);
		}
		varargNamesInFunction.removeLast();
		out.removeIndent();
		out.printLine("});");
	}

	private void writeLoadConstant(Step step) {
		var c = ((LoadConstant) step).getConstant();
		var target = ((LoadConstant) step).getTarget().getName();
		if (c == null) {
			c = "null";
		} else if (c instanceof String) {
			c = '"' + c.toString() + '"';
		}
		out.printLine("Dynamic ", target, " = Dynamic.of(", c, ");");
	}

	private void writeMakeTable(Step step) {
		var mt = (MakeTable) step;
		var result = mt.getResult();
		var map = new HashMap<>(mt.getValues());
		Optional<Entry<Register, Register>> vararg = map.entrySet().stream()
				.filter(e -> e.getValue().isVararg())
				.findAny();
		String list = map.entrySet().stream().map(e -> e.getKey() + ", " + e.getValue()).collect(Collectors.joining());
		vararg.ifPresentOrElse(o -> {
			var offset = o.getKey();
			var value = o.getValue().getName();
			out.printLine("Dynamic ", result, " = TableOps.createWithVararg(", offset, ", ", value, ", ", list, ");");
		}, () -> {
			out.printLine("Dynamic ", result, " = TableOps.create(", list, ");");
		});
	}

	private void writeOperator(Step step) {
		var op = (Operator) step;
		var a = Optional.ofNullable(op.getA()).map(Register::getName).orElse("");
		var b = op.getB().getName();
		var symbol = '"' + op.getSymbol() + '"';
		var target = op.getTarget().getName();
		out.printLine("Dynamic ", target, " = DynamicOps.operator(", a, ", ", symbol, ", ", b, ");");
	}

	private void writeRead(Step step) {
		var read = (Read) step;
		switch (read.getSourceInfo().getMode()) {
			case LOCAL: {
				out.printLine("Dynamic ", read.getRegister(), " = ", read.getName(), ";");
				break;
			}
			case UPVALUE: {
				out.printLine("Dynamic ", read.getRegister(), " = ", read.getName(), ".get();");
				break;
			}
			case GLOBAL: {
				out.printLine("Dynamic ", read.getRegister(), " = ", "_ENV.get().get(\"", read.getName(), "\");");
				break;
			}
			default:
				throw new AssertionError();
		}
	}

	private void writeReturn(Step step) {
		var ret = (Return) step;
		var regs = ret.getRegisters();
		out.printLine("return MultiValue.of(", commaList(regs), ");");
	}

	private void writeGetVarargs(Step step) throws CompilationFailure {
		var g = (GetVarargs) step;
		var varargsName = varargNamesInFunction.getLast();
		if (varargsName.isPresent()) {
			var varargs = varargsName.get();
			out.printLine("MultiValue ", g.getTo().getName(), " = ", varargs, ";");
		} else {
			illegalVarargUsage();
		}
	}

	private void writeSelect(Step step) {
		var select = (Select) step;
		var n = select.getN();
		var vararg = select.getVarargs().getName();
		var target = select.getOut().getName();
		out.printLine(target, " = MultiOps.select(", vararg, ", ", n, ");");
	}

	private void writeTableRead(Step step) {
		var read = (TableRead) step;
		out.printLine("Dynamic ", read.getOut(), " = DynamicOps.index(", read.getTable(), ", ", read.getKey(), ");");
	}

	private void writeTableWrite(Step step) {
		var write = (TableWrite) step;
		out.printLine("DynamicOps.setIndex(", write.getField().getTable(), ", ", write.getField().getKey(), ", ", write.getValue(), ");");
	}

	private void writeWrite(Step step) {
		var write = (Write) step;
		switch (write.getTarget().getMode()) {
			case LOCAL: {
				out.printLine(write.getTarget(), " = ", write.getSource().getName(), ";");
				break;
			}
			case UPVALUE: {
				out.printLine(write.getTarget(), ".set(", write.getSource().getName(), ");");
				break;
			}
			case GLOBAL: {
				out.printLine("_ENV.get().set(", write.getTarget(), ", ", write.getSource().getName(), ");");
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
		var name = loop.getVarName();
		var loopName = RegisterFactory.create().getName();
		out.printLine("for(double ", loopName, " = ", from, "; ", loopName, " <= ", to, "; ", loopName, "++) {");
		out.addIndent();
		out.printLine("Dynamic ", name, " = Dynamic.of(", loopName, ");");
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
			out.printLine("Dynamic ", ((Declare) step).getName(), ";");
		} else {
			out.printLine("Upvalue ", declare.getName(), " = Upvalue.create();");
		}
	}

	private void writeComment(Step step) {
		out.printLine("// ", ((Comment) step).getText());
	}

	private void writeCall(Step step) {
		var call = (Call) step;
		var function = call.getFunction().getName();
		var variable = call.getFunction().getName();
		var args = commaList(call.getArgs());
		out.printLine("Dynamic ", variable, " = DynamicOps.call(", function, ", MultiValue.of(", args, "));");
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

	private void writeBranch(Step step) throws CompilationFailure {
		var branch = (Branch) step;
		out.printLine("if(DynamicOps.isTruthy(", branch.getCondition().getName(), ") {");
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
		for (var step : block.steps()) {
			write(step);
		}
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
