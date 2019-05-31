package optic.lua.io;

import nl.bigo.luaparser.*;
import optic.lua.messages.CompilationFailure;
import optic.lua.util.Trees;
import org.antlr.runtime.*;
import org.antlr.runtime.tree.CommonTree;
import org.slf4j.*;

/**
 * Converts Lua source code into an abstact syntax tree using {@link #parse(CharStream)}.
 */
final class LuaSourceParser {
	private static final Logger log = LoggerFactory.getLogger(LuaSourceParser.class);

	CommonTree parse(CharStream charStream) throws CompilationFailure {
		try {
			var lexer = new Lua53Lexer(charStream);
			var parser = new Lua53Parser(new CommonTokenStream(lexer));
			return parser.parse().getTree();
		} catch (RecognitionException e) {
			logParsingError(e);
			throw new CompilationFailure();
		} catch (RuntimeException e) {
			if (e.getCause() instanceof RecognitionException) {
				logParsingError((RecognitionException) e.getCause());
			} else {
				logOtherError(e);
			}
			throw new CompilationFailure();
		}
	}

	private void logOtherError(RuntimeException e) {
		log.error("Parsing failed due to error: {}", e.getMessage());
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
			message.append(" (expected ");
			message.append(Trees.reverseLookupName(mte.expecting));
			message.append(", got ");
			message.append(Trees.reverseLookupName(mte.getUnexpectedType()));
			message.append(')');
		} else {
			message.append(" (unexpected ");
			message.append(Trees.reverseLookupName(e.getUnexpectedType()));
			message.append(')');
		}
		log.error(message.toString());
	}
}
