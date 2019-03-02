package optic.lua.io;

import nl.bigo.luaparser.*;
import optic.lua.io.CompilerPlugin.Factory;
import optic.lua.asm.*;
import optic.lua.messages.*;
import optic.lua.util.Trees;
import org.antlr.runtime.*;
import org.antlr.runtime.tree.CommonTree;

import java.util.*;

final class SingleSourceCompiler {
	private final CodeSource source;
	private final SyntaxTreeFlattener flattener = MutableFlattener::flatten;
	private final Context context;
	private final List<Factory> pluginFactories;

	public SingleSourceCompiler(Context context, CodeSource source, List<Factory> pluginFactories) {
		this.source = source;
		this.pluginFactories = pluginFactories;
		this.context = context.withSource(source).withPhase(Phase.COMPILING);
	}

	public void run() throws CompilationFailure {
		CharStream charStream = source.newCharStream(context.withPhase(Phase.READING));
		CommonTree ast = parse(charStream);
		AsmBlock steps = flattener.flatten(ast, context.withPhase(Phase.FLATTENING));
		for (var factory : pluginFactories) {
			var plugin = factory.create(steps, context.withPhase(Phase.COMPILING));
			steps = plugin.apply();
		}
	}

	private CommonTree parse(CharStream charStream) throws CompilationFailure {
		try {
			var lexer = new Lua52Lexer(charStream);
			var parser = new Lua52Parser(new CommonTokenStream(lexer));
			return parser.parse().getTree();
		} catch (RecognitionException e) {
			var msg = parsingError(e);
			context.reporter().report(msg);
			throw new CompilationFailure(Tag.BAD_INPUT, Tag.PARSER);
		} catch (RuntimeException e) {
			if (e.getCause() instanceof RecognitionException) {
				var msg = parsingError((RecognitionException) e.getCause());
				context.reporter().report(msg);
				throw new CompilationFailure(Tag.BAD_INPUT, Tag.PARSER);
			}
			var msg = Message.createError(Objects.toString(e.getMessage(), "(no message)"));
			if (e.getMessage() == null) {
				msg.setCause(e);
			}
			context.reporter().report(msg);
			throw new CompilationFailure(Tag.BAD_INPUT, Tag.PARSER);
		}
	}

	private Message parsingError(RecognitionException e) {
		var message = new StringBuilder("Invalid syntax ");
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
		var error = Message.create(message.toString());
		error.setLine(e.line);
		error.setColumn(e.charPositionInLine);
		error.setLevel(Level.ERROR);
		error.setPhase(Phase.PARSING);
		error.setSource(source);
		error.addTags(Tag.BAD_INPUT, Tag.PARSER);
		return error;
	}
}
