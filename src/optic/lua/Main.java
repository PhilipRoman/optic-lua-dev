package optic.lua;

import optic.lua.io.*;
import optic.lua.messages.*;
import optic.lua.runtime.LuaContext;
import org.slf4j.*;
import picocli.CommandLine;

import java.nio.file.Path;
import java.util.*;

public final class Main {
	private static final Logger log = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) {
		var options = new Options();
		var bundleCompiler = new BundleCompiler(options);

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

		Set<Path> sources = new HashSet<>(opticLua.sources);
		if (opticLua.mainSource != null) {
			sources.add(opticLua.mainSource);
		}
		Bundle bundle = null;
		try {
			bundle = bundleCompiler.compile(sources);
		} catch (CompilationFailure e) {
			log.error("Some tasks failed");
			System.exit(1);
		}

		if (opticLua.interactiveShell) {
			// interactive session
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
			new Runner(options).run(method, LuaContext.create(bundle), List.of());
			if (options.get(StandardFlags.SHOW_TIME)) {
				logDurationInfo(System.nanoTime() - start);
			}
		}
	}

	private static void logDurationInfo(long nanos) {
		log.info("Program took {}ms", (nanos / (int) 1e6));
	}
}
