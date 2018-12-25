package optic.lua;

import nl.bigo.luaparser.*;
import optic.lua.asm.*;
import optic.lua.codegen.CodeOutput;
import optic.lua.messages.*;
import org.antlr.runtime.*;
import org.antlr.runtime.tree.CommonTree;

import java.util.Set;

public final class Pipeline {
	private final CodeSource source;
	private final SyntaxTreeFlattener flattener;
	private final Context context;
	private final CodeOutput output;

	public Pipeline(Set<Option> options, MessageReporter reporter, CodeSource source, SyntaxTreeFlattener flattener, CodeOutput output) {
		this.source = source;
		this.flattener = flattener;
		this.context = new Context(Set.copyOf(options), reporter.withSource(source));
		this.output = output;
	}

	public void run() throws CompilationFailure {
		MessageReporter reporter = context.reporter();
		long startTime = System.nanoTime();
		CharStream charStream = source.newCharStream(context.withPhase(Phase.READING));
		CommonTree ast = parse(charStream);
		AsmBlock steps = flattener.flatten(ast, context.withPhase(Phase.FLATTENING));
		output.output(steps, context.withPhase(Phase.CODEGEN));
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
			context.reporter().report(msg);
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
