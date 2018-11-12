package optic.lua;

import nl.bigo.luaparser.*;
import optic.lua.codegen.CodeOutput;
import optic.lua.messages.*;
import optic.lua.ssa.*;
import org.antlr.runtime.*;
import org.antlr.runtime.tree.CommonTree;

import java.util.List;

public final class Pipeline {
	private final CodeSource source;
	private final SyntaxTreeFlattener ssa;
	private final MessageReporter reporter;
	private final CodeOutput output;

	public Pipeline(CodeSource source, SyntaxTreeFlattener ssa, MessageReporter reporter, CodeOutput output) {
		this.source = source;
		this.ssa = ssa;
		this.reporter = reporter.withSource(source);
		this.output = output;
	}

	public void run() throws CompilationFailure {
		long startTime = System.nanoTime();
		CharStream charStream = source.newCharStream(reporter.withPhase(Phase.READING));
		CommonTree ast = parse(charStream);
		List<Step> steps = ssa.flatten(ast, reporter.withPhase(Phase.FLATTENING));
		output.output(steps, reporter.withPhase(Phase.CODEGEN));
		long endTime = System.nanoTime();
		reporter.report(durationInfo(endTime - startTime));
	}

	private CommonTree parse(CharStream charStream) throws CompilationFailure {
		try {
			var lexer = new Lua52Lexer(charStream);
			var parser = new Lua52Parser(new CommonTokenStream(lexer));
			return parser.parse().getTree();
		} catch (RecognitionException e) {
			var msg = parsingError(e);
			reporter.report(msg);
			throw new CompilationFailure();
		}
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
		var error = Message.create("Pipeline took " + (nanos / (int) 1e6) + " ms");
		error.setLevel(Level.INFO);
		error.setPhase(Phase.CODEGEN);
		error.setSource(source);
		return error;
	}
}
