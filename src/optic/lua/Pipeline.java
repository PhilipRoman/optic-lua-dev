package optic.lua;

import nl.bigo.luaparser.*;
import optic.lua.asm.*;
import optic.lua.messages.*;
import org.antlr.runtime.*;
import org.antlr.runtime.tree.CommonTree;

import java.util.*;
import java.util.concurrent.*;

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
		ExecutorService background = Executors.newCachedThreadPool();
		Collection<Future<?>> running = new ArrayList<>(4);
		for (var factory : pluginFactories) {
			long startTime = System.nanoTime();
			var plugin = factory.create(steps, context.withPhase(Phase.COMPILING));
			if(context.options().get(StandardFlags.PARALLEL) && plugin.concurrent()) {
				reporter.report(Message.createInfo("Applying plugin " + plugin + " in background"));
				var future = background.submit(() -> {
					plugin.apply();
					reporter.report(durationInfo(plugin, System.nanoTime() - startTime));
					return null;
				});
				running.add(future);
			} else {
				reporter.report(Message.createInfo("Applying plugin " + plugin));
				steps = plugin.apply();
				reporter.report(durationInfo(plugin, System.nanoTime() - startTime));
			}
		}

		for(var future : running) {
			try {
				future.get();
			} catch (InterruptedException e) {
				throw new RuntimeException("Concurrent plugins should not be interrupted!");
			} catch (ExecutionException e) {
				var msg = Message.createError("Plugin failed!", e.getCause());
				reporter.report(msg);
				throw new CompilationFailure();
			}
		}
		// Shut down the executor to allow the virtual machine to terminate
		background.shutdownNow();
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

	private Message durationInfo(Object action, long nanos) {
		Objects.requireNonNull(action);
		var error = Message.create(action + " took " + (nanos / (int) 1e6) + " ms");
		error.setLevel(Level.INFO);
		error.setPhase(Phase.CODEGEN);
		error.setSource(source);
		return error;
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
