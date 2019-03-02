package optic.lua;

import nl.bigo.luaparser.*;
import optic.lua.asm.*;
import optic.lua.messages.*;
import optic.lua.util.Trees;
import org.antlr.runtime.*;
import org.antlr.runtime.tree.CommonTree;

import java.util.*;

public final class Pipeline {
	private final CodeSource source;
	private final SyntaxTreeFlattener flattener = MutableFlattener::flatten;
	private final Context context;
	private final List<CompilerPlugin.Factory> pluginFactories = new ArrayList<>();
	private final EnumMap<Moment, Long> timing = new EnumMap<>(Moment.class);

	public Pipeline(Options options, MessageReporter reporter, CodeSource source) {
		this.source = source;
		this.context = new Context(options, reporter.withSource(source));
	}

	public void run() throws CompilationFailure {
		MessageReporter reporter = context.reporter();

		registerTime(Moment.START);
		CharStream charStream = source.newCharStream(context.withPhase(Phase.READING));
		CommonTree ast = parse(charStream);
		registerTime(Moment.FINISH_PARSING);
		reporter.report(durationInfo("Parsing", getTime(Moment.START, Moment.FINISH_PARSING)));

		registerTime(Moment.START_FLATTENING);
		AsmBlock steps = flattener.flatten(ast, context.withPhase(Phase.FLATTENING));
		registerTime(Moment.FINISH_FLATTENING);
		reporter.report(durationInfo("Flattening", getTime(Moment.START_FLATTENING, Moment.FINISH_FLATTENING)));

		registerTime(Moment.START_PLUGINS);
		for (var factory : pluginFactories) {
			long startTime = System.nanoTime();
			var plugin = factory.create(steps, context.withPhase(Phase.COMPILING));
			reporter.report(Message.createDebug("Applying plugin " + plugin));
			steps = plugin.apply();
			reporter.report(durationInfo(plugin, System.nanoTime() - startTime));
		}
		registerTime(Moment.FINISH_PLUGINS);

		reporter.report(durationInfo("Plugins", getTime(Moment.START_PLUGINS, Moment.FINISH_PLUGINS)));

		registerTime(Moment.FINISH);
		reporter.report(durationInfo("Pipeline", getTime(Moment.START, Moment.FINISH)));
	}

	public void registerPlugin(CompilerPlugin.Factory factory) {
		pluginFactories.add(factory);
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

	private Message durationInfo(Object action, long nanos) {
		Objects.requireNonNull(action);
		var msg = Message.create(action + " took " + (nanos / (int) 1e6) + " ms");
		msg.setLevel(Level.DEBUG);
		msg.setPhase(Phase.CODEGEN);
		msg.addTag(Tag.STATISTICS);
		msg.setSource(source);
		return msg;
	}

	private void registerTime(Moment moment) {
		assert !timing.containsKey(moment);
		timing.put(moment, System.nanoTime());
	}

	private long getTime(Moment from, Moment to) {
		long t1 = timing.get(from);
		long t2 = timing.get(to);
		if (t1 > t2) {
			throw new IllegalArgumentException("Moment " + to + " is not later than " + from);
		}
		return t2 - t1;
	}

	private enum Moment {
		START, FINISH_PARSING, START_FLATTENING, FINISH_FLATTENING, START_PLUGINS, FINISH_PLUGINS, FINISH
	}
}
