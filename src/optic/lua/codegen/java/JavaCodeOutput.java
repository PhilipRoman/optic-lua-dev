package optic.lua.codegen.java;

import optic.lua.CompilerPlugin;
import optic.lua.asm.*;
import optic.lua.asm.instructions.*;
import optic.lua.codegen.TemplateOutput;
import optic.lua.messages.*;
import optic.lua.optimization.TypeStatus;
import optic.lua.util.*;
import org.jetbrains.annotations.NotNull;

import java.io.*;
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
public class JavaCodeOutput extends StepVisitor<Void> implements CompilerPlugin  {
	private final TemplateOutput out;
	private final AsmBlock block;
	private final Context context;
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

	public Void visitToNumber(@NotNull ToNumber toNumber) {
		var from = toNumber.getSource();
		var to = toNumber.getTarget();
		out.printLine("double ", to, " = StandardLibrary.toNumber(", from, ");");
		return null;
	}

	public Void visitFunctionLiteral(@NotNull FunctionLiteral function) throws CompilationFailure {
		var target = function.getAssignTo().getName();
		var params = function.getParams().list();
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
		if (!function.getParams().hasVarargs()) {
			varargNamesInFunction.addLast(Optional.empty());
		}
		out.printLine("if(1 == 1) {");
		visitAll(function.getBody().steps());
		out.printLine("}");
		out.printLine("return ListOps.empty();");
		varargNamesInFunction.removeLast();
		out.removeIndent();
		out.printLine("}};");
		return null;
	}

	public Void visitLoadConstant(@NotNull LoadConstant loadConstant) {
		var c = loadConstant.getConstant();
		var target = loadConstant.getTarget().getName();
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
		return null;
	}

	public Void visitMakeTable(@NotNull MakeTable makeTable) {
		var result = makeTable.getResult();
		var map = new HashMap<>(makeTable.getValues());
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
		return null;
	}

	public Void visitOperator(@NotNull Operator op) {
		var target = op.getTarget().getName();
		var a = Optional.ofNullable(op.getA()).map(Register::getName).orElse("");
		var b = op.getB().getName();
		if (Set.of("*", "+", "-", "/").contains(op.getSymbol())
				&& op.getA() != null
				&& op.getA().status() == TypeStatus.NUMBER
				&& op.getB().status() == TypeStatus.NUMBER
				&& op.getTarget().status() == TypeStatus.NUMBER) {
			writeDebugComment("Fast numeric operation between " + op.getA().toDebugString() + " and " + op.getB().toDebugString());
			out.printLine("double ", target, " = ", a, " ", op.getSymbol(), " ", b, ";");
			return null;
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
		return null;
	}

	public Void visitRead(@NotNull Read read) {
		writeDebugComment("read " + read.getSourceInfo().toDebugString() + " to " + read.getRegister().toDebugString());
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
		return null;
	}

	public Void visitReturn(@NotNull Return ret) {
		var regs = ret.getRegisters();
		if (!regs.isEmpty() && regs.get(regs.size() - 1).isVararg()) {
			var varargs = regs.get(regs.size() - 1).getName();
			var values = new ArrayList<>(regs);
			values.remove(values.size() - 1);
			out.printLine("return ListOps.concat(", varargs, ", ", commaList(values), ");");
		} else {
			out.printLine("return ListOps.create(", commaList(regs), ");");
		}
		return null;
	}

	public Void visitGetVarargs(@NotNull GetVarargs getVarargs) throws CompilationFailure {
		var varargsName = varargNamesInFunction.getLast();
		if (varargsName.isPresent()) {
			var varargs = varargsName.get();
			out.printLine("Object[] ", getVarargs.getTo().getName(), " = ", varargs, ";");
		} else {
			illegalVarargUsage();
		}
		return null;
	}

	public Void visitSelect(@NotNull Select select) {
		var n = select.getN();
		var vararg = select.getVarargs().getName();
		var target = select.getOut().getName();
		out.printLine("Object ", target, " = ListOps.get(", vararg, ", ", n, ");");
		return null;
	}

	public Void visitTableRead(@NotNull TableRead tableRead) {
		out.printLine("Object ", tableRead.getOut(), " = DynamicOps.index(", tableRead.getTable(), ", ", tableRead.getKey(), ");");
		return null;
	}

	public Void visitTableWrite(@NotNull TableWrite tableWrite) {
		out.printLine("DynamicOps.setIndex(", tableWrite.getField().getTable(), ", ", tableWrite.getField().getKey(), ", ", tableWrite.getValue(), ");");
		return null;
	}

	public Void visitWrite(@NotNull Write write) {
		writeDebugComment("writing " + write.getSource().toDebugString() + " to " + write.getTarget().toDebugString());
		switch (write.getTarget().getMode()) {
			case LOCAL: {
				if (write.getTarget().status() == TypeStatus.NUMBER && write.getSource().status() != TypeStatus.NUMBER)
					out.printLine(write.getTarget(), " = StandardLibrary.toNumber(", write.getSource().getName(), ");");
				else
					out.printLine(write.getTarget(), " = ", write.getSource().getName(), ";");
				return null;
			}
			case UPVALUE: {
				out.printLine(write.getTarget(), ".set(", write.getSource().getName(), ");");
				return null;
			}
			case GLOBAL: {
				out.printLine("EnvOps.set(_ENV, \"", write.getTarget(), "\", ", write.getSource().getName(), ");");
				return null;
			}
			default:
				throw new AssertionError();
		}
	}

	public Void visitForRangeLoop(@NotNull ForRangeLoop loop) throws CompilationFailure {
		var from = loop.getFrom().getName();
		var to = loop.getTo().getName();
		var counter = loop.getCounter();
		var counterName = "i_" + counter.getName();
		out.printLine("for(double ", counterName, " = ", from, "; ", counterName, " <= ", to, "; ", counterName, "++) {");
		out.addIndent();
		String counterTypeName = loop.getCounter().status() == TypeStatus.NUMBER ? "double" : "Object";
		out.printLine(counterTypeName, " ", counter.getName(), " = ", counterName, ";");
		for (Step s : loop.getBlock().steps()) {
			visit(s);
		}
		out.removeIndent();
		out.printLine("}");
		return null;
	}

	public Void visitDeclare(@NotNull Declare declare) {
		assert declare.getVariable().getMode() != VariableMode.GLOBAL;
		if (declare.getVariable().getMode() == VariableMode.LOCAL) {
			String finalPrefix = declare.getVariable().isFinal() ? "final " : "";
			if (declare.getVariable().status() == TypeStatus.NUMBER) {
				out.printLine(finalPrefix, "double ", declare.getName(), ";");
			} else {
				out.printLine(finalPrefix, "Object ", declare.getName(), ";");
			}
		} else {
			out.printLine("final UpValue ", declare.getName(), " = UpValue.create();");
		}
		return null;
	}

	public Void visitComment(@NotNull Comment comment) {
		if (context.options().contains(Option.KEEP_COMMENTS)) {
			out.printLine("// ", comment.getText());
		}
		return null;
	}

	private void writeDebugComment(String comment) {
		if (context.options().contains(Option.DEBUG_COMMENTS)) {
			out.printLine("// ", comment);
		}
	}

	public Void visitCall(@NotNull Call call) {
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
		return null;
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

	public Void visitBranch(@NotNull Branch branch) throws CompilationFailure {
		out.printLine("if(DynamicOps.isTrue(", branch.getCondition().getName(), ")) {");
		out.addIndent();
		visitAll(branch.getBody().steps());
		out.removeIndent();
		out.printLine("}");
		return null;
	}

	public Void visitBlock(@NotNull Block block) throws CompilationFailure {
		out.printLine("{");
		out.addIndent();
		visitAll(block.getSteps().steps());
		out.removeIndent();
		out.printLine("}");
		return null;
	}

	private JavaCodeOutput(PrintStream out, AsmBlock block, Context context) {
		this.out = new TemplateOutput(out);
		this.block = block;
		this.context = context;
	}

	public static CompilerPlugin.Factory writingTo(OutputStream stream) {
		PrintStream printStream = new PrintStream(new BufferedOutputStream(stream));
		return (steps, context) -> new JavaCodeOutput(printStream, steps, context);
	}

	private void execute() throws CompilationFailure {
		var msg = Message.create("Java code output still in development!");
		msg.setLevel(Level.WARNING);
		context.reporter().report(msg);
		out.printLine("import optic.lua.runtime.*;");
		out.printLine("static Object[] main(final UpValue _ENV, Object[] args) { if(1 == 1) {");
		out.addIndent();
		visitAll(block.steps());
		out.removeIndent();
		out.printLine("} return ListOps.empty(); }");
		out.printLine("main(UpValue.create(EnvOps.createEnv()), new Object[0]);");
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
	public Void defaultValue(@NotNull Step x) {
		return null;
	}
}
