package optic.lua;

import optic.lua.codegen.java.JavaCodeOutput;
import optic.lua.io.Compiler;
import optic.lua.io.*;
import optic.lua.messages.*;
import org.slf4j.*;

import java.nio.file.*;

import static optic.lua.messages.StandardFlags.*;

public class Main {
	private static final Logger log = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) throws Exception {
		String fileName = args.length > 0 ? args[0] : System.getProperty("optic.source");
		if (fileName == null) {
			var shell = new InteractiveShell(System.in, System.out, System.err);
			shell.run();
			return;
		}
		int nTimes = Integer.parseInt(System.getProperty("optic.n", "10"));
		boolean useSSA = Boolean.valueOf(System.getProperty("optic.ssa", "true"));
		boolean useLoopSplit = Boolean.valueOf(System.getProperty("optic.loops", "true"));
		var codeSource = CodeSource.ofFile(fileName);
		var temp = Files.createTempFile("optic_lua_", ".java");
		var options = new Options();
		options.set(SSA_SPLIT, useSSA);
		options.set(LOOP_SPLIT, useLoopSplit);
		options.disable(KEEP_COMMENTS);
		options.enable(DEBUG_COMMENTS);
		options.enable(PARALLEL);
		options.enable(VERIFY);
		options.set(INDENT, "\t");
		MessageReporter reporter = new LogMessageReporter(log, new SimpleMessageFormat());
		var pipeline = new Pipeline(
				options,
				reporter,
				codeSource
		);
		pipeline.registerPlugin(JavaCodeOutput.writingTo(Files.newOutputStream(temp)));
		try {
			pipeline.run();
		} catch (CompilationFailure e) {
			System.err.print("Failed!");
			e.printStackTrace();
			System.exit(1);
		}
		Files.copy(temp, Paths.get("out.java"), StandardCopyOption.REPLACE_EXISTING);
		new Compiler(new Context(options, reporter)).run(Files.newInputStream(temp), nTimes);
	}
}
