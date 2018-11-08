package optic.lua;

import nl.bigo.luaparser.*;
import optic.lua.codegen.CodeOutput;
import optic.lua.messages.*;
import optic.lua.ssa.*;
import org.antlr.runtime.*;
import org.antlr.runtime.tree.CommonTree;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Pipeline<Result> {
	private final CodeSource source;
	private final SSATranslator ssa;
	private final MessageReporter reporter;
	private final CodeOutput<Result> output;

	public Pipeline(CodeSource source, SSATranslator ssa, MessageReporter reporter, CodeOutput<Result> output) {
		this.source = source;
		this.ssa = ssa;
		this.reporter = reporter.withSource(source);
		this.output = output;
	}

	public Result run() throws CompilationFailure {
		long startTime = System.nanoTime();
		@NotNull final CommonTree ast;
		try {
			var lexer = new Lua52Lexer(source.charStream());
			var parser = new Lua52Parser(new CommonTokenStream(lexer));
			ast = parser.parse().getTree();
		} catch (RecognitionException e) {
			var msg = parsingError(e);
			reporter.report(msg);
			throw new CompilationFailure();
		}
		@NotNull final List<Step> steps;
		try {
			steps = ssa.translate(ast, reporter.withPhase(Phase.FLATTENING));
		} catch (CompilationFailure e) {
			throw new CompilationFailure();
		}
		final Result result;
		try {
			result = output.output(steps, reporter.withPhase(Phase.CODEGEN));
		} catch (CompilationFailure e) {
			throw new CompilationFailure();
		}
		long endTime = System.nanoTime();
		reporter.report(durationInfo(endTime - startTime));
		return result;
	}

	private Message parsingError(RecognitionException e) {
		var error = Message.create("Invalid syntax");
		error.setLine(e.line);
		error.setColumn(e.charPositionInLine);
		error.setCause(e);
		error.setLevel(Level.ERROR);
		error.setPhase(Phase.PARSING);
		error.setSource(source);
		return error;
	}

	private Message durationInfo(long nanos) {
		var error = Message.create("Pipeline took " + nanos / 1000_000_000f + " seconds");
		error.setLevel(Level.INFO);
		error.setPhase(Phase.CODEGEN);
		error.setSource(source);
		return error;
	}
}
