package optic.lua;

import optic.lua.io.*;
import optic.lua.messages.*;
import optic.lua.runtime.LuaContext;
import org.slf4j.*;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
	private static final Logger log = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) throws Exception {
		var options = new Options();
		MessageReporter reporter = new LogMessageReporter(log, new SimpleMessageFormat())
				.filter(Level.HINT);
		var context = new Context(options, reporter);
		var bundleCompiler = new BundleCompiler(context);
		var allSamples = Objects.requireNonNull(new File("samples").listFiles());
		Bundle bundle = bundleCompiler.compile(Arrays.stream(allSamples).map(File::toPath).collect(Collectors.toList()));
		if (Boolean.getBoolean("optic.shell")) {
			// interactive session
			var shell = new InteractiveShell(System.in, System.out, System.err, bundle);
			shell.run();
			return;
		}
		// run a file
		String fileName = System.getProperty("optic.source");
		int nTimes = Integer.parseInt(System.getProperty("optic.n", "10"));
		for (int i = 0; i < nTimes; i++) {
			long start = System.nanoTime();
			bundle.doFile(fileName, LuaContext.create(bundle), List.of());
			context.reporter().report(durationInfo(System.nanoTime() - start));
		}
	}

	private static Message durationInfo(long nanos) {
		var msg = Message.create("Program took " + (nanos / (int) 1e6) + " ms");
		msg.setLevel(Level.HINT);
		msg.setPhase(Phase.RUNTIME);
		msg.addTag(Tag.STATISTICS);
		return msg;
	}
}
