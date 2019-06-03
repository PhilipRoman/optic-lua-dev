package optic.lua;

import optic.lua.io.*;
import optic.lua.messages.*;
import optic.lua.runtime.LuaContext;
import optic.lua.runtime.invoke.InstrumentedCallSiteFactory;
import org.slf4j.*;
import picocli.CommandLine;

import java.nio.file.Path;
import java.util.*;

/**
 * The entry point for running the compiler as a standalone program.
 */
public final class Main {
	private static final Logger log = LoggerFactory.getLogger(Main.class);

	private Main() {
	}

	public static void main(String[] args) {
		// command line parser
		OpticLua opticLua = new OpticLua();
		CommandLine commandLine = new CommandLine(opticLua);
		commandLine.parse(args);

		if (commandLine.isUsageHelpRequested()) {
			commandLine.usage(System.err);
			return;
		}

		if (commandLine.isVersionHelpRequested()) {
			commandLine.printVersionHelp(System.err);
			return;
		}

		var options = new Options();
		// store advanced option flags from command line into options
		opticLua.compilerFlags.forEach((option, enabled) -> {
			if (enabled)
				options.enable(option);
			else
				options.disable(option);
		});
		if (opticLua.javaCodeDump)
			options.enable(StandardFlags.DUMP_JAVA);
		if (opticLua.showTime)
			options.enable(StandardFlags.SHOW_TIME);
		if (opticLua.showRtStats)
			options.enable(StandardFlags.SHOW_RT_STATS);
		if (opticLua.generateClasses)
			options.enable(StandardFlags.GENERATE_CLASSES);

		// a pool of source files to compile
		Set<Path> sources = new HashSet<>(opticLua.sources);
		// include the file to run
		if (opticLua.mainSource != null) {
			sources.add(opticLua.mainSource);
		}
		var bundleCompiler = new BundleCompiler(options);
		final Bundle bundle;
		try {
			bundle = bundleCompiler.compile(sources);
		} catch (CompilationFailure e) {
			log.error("Some tasks failed");
			System.exit(1);
			return; // for control flow analysis tools
		}

		if (opticLua.interactiveShell) {
			// enter interactive session after compiling the files
			var shell = new InteractiveShell(System.in, System.out, System.err, bundle, options);
			shell.run();
			return;
		}

		if (opticLua.sources.isEmpty() && opticLua.mainSource == null) {
			commandLine.usage(System.err);
			return;
		}

		if (opticLua.mainSource == null) {
			log.info("Nothing to do, exiting!");
			return;
		}

		String fileName = opticLua.mainSource.toString();
		int nTimes = opticLua.nTimes;
		var method = bundle.findCompiled(fileName).orElseThrow(() -> new NoSuchElementException(fileName));
		for (int i = 0; i < nTimes; i++) {
			long start = System.nanoTime();
			var luaContext = LuaContext.create(bundle);
			// runtime stats can only be shown if the Lua context uses a special call site factory
			if(options.get(StandardFlags.SHOW_RT_STATS)) {
				luaContext.callSiteFactory = new InstrumentedCallSiteFactory();
			}
			new Runner(options).run(method, luaContext, List.of());
			if (options.get(StandardFlags.SHOW_TIME)) {
				logDurationInfo(System.nanoTime() - start);
			}
		}
	}

	private static void logDurationInfo(long nanos) {
		log.info("Program took {}ms", (nanos / (int) 1e6));
	}
}
