package optic.lua.io;

import nl.bigo.luaparser.*;
import optic.lua.asm.*;
import optic.lua.io.CompilerPlugin.Factory;
import optic.lua.messages.*;
import optic.lua.util.Trees;
import org.antlr.runtime.*;
import org.antlr.runtime.tree.CommonTree;
import org.slf4j.*;

import java.util.List;

final class SingleSourceCompiler {
	private static final Logger log = LoggerFactory.getLogger(SingleSourceCompiler.class);

	private final CodeSource source;
	private final SyntaxTreeFlattener flattener = MutableFlattener::flatten;
	private final Options options;
	private final List<Factory> pluginFactories;

	public SingleSourceCompiler(Options options, CodeSource source, List<Factory> pluginFactories) {
		this.source = source;
		this.pluginFactories = pluginFactories;
		this.options = options;
	}

	public void run() throws CompilationFailure {
		CharStream charStream = source.newCharStream(options);
		CommonTree ast = parse(charStream);
		AsmBlock steps = flattener.flatten(ast, options);
		for (var factory : pluginFactories) {
			var plugin = factory.create(steps, options);
			steps = plugin.apply();
		}
	}

	private CommonTree parse(CharStream charStream) throws CompilationFailure {
		try {
			var lexer = new Lua52Lexer(charStream);
			var parser = new Lua52Parser(new CommonTokenStream(lexer));
			return parser.parse().getTree();
		} catch (RecognitionException e) {
			logParsingError(e);
			throw new CompilationFailure();
		} catch (RuntimeException e) {
			if (e.getCause() instanceof RecognitionException) {
				logParsingError((RecognitionException) e.getCause());
			}
			throw new CompilationFailure();
		}
	}

	private void logParsingError(RecognitionException e) {
		var message = new StringBuilder("Invalid syntax ");
		message.append("(");
		message.append(e.line);
		message.append(':');
		message.append(e.charPositionInLine);
		message.append(')');
		if (e instanceof MismatchedTokenException) {
			var mte = ((MismatchedTokenException) e);
			message.append("(expected ");
			message.append(Trees.reverseLookupName(mte.expecting));
			message.append(", got ");
			message.append(Trees.reverseLookupName(mte.getUnexpectedType()));
			message.append(')');
		} else {
			message.append("(unexpected ");
			message.append(Trees.reverseLookupName(e.getUnexpectedType()));
			message.append(')');
		}
		log.error(message.toString());
	}
}
