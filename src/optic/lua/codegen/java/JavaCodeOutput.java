package optic.lua.codegen.java;

import optic.lua.codegen.CodeOutput;
import optic.lua.messages.*;
import optic.lua.asm.Step;

import java.io.*;
import java.util.List;

public class JavaCodeOutput {
	private final PrintStream out;
	private final List<Step> steps;
	private final MessageReporter reporter;
	private int indent = 0;

	private JavaCodeOutput(PrintStream out, List<Step> steps, MessageReporter reporter) {
		this.out = out;
		this.steps = steps;
		this.reporter = reporter;
	}

	public static CodeOutput writingTo(OutputStream stream) {
		PrintStream printStream = stream instanceof PrintStream
				? (PrintStream) stream
				: new PrintStream(stream);
		return (steps, reporter) -> new JavaCodeOutput(printStream, steps, reporter).execute();
	}

	private void execute() throws CompilationFailure {
		var msg = Message.create("Java code output not yet supported!");
		msg.setLevel(Level.ERROR);
		reporter.report(msg);
		throw new CompilationFailure();
	}

	private void addIndent() {
		indent++;
	}

	private void removeIndent() {
		indent--;
		if (indent < 0) throw new IllegalStateException();
	}

	private void endLine() {
		out.println();
	}

	private void printf(String pattern, Object... args) {
		out.printf(pattern, args);
	}
}
