package optic.lua;

import optic.lua.codegen.java.JavaCodeOutput;
import optic.lua.files.Compiler;
import optic.lua.messages.*;
import optic.lua.verify.*;
import org.slf4j.*;

import java.nio.file.Files;

import static optic.lua.messages.StandardFlags.*;

public class Main {
	private static final Logger log = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) throws Exception {
		var codeSource = CodeSource.ofFile("samples/anon-functions.lua");
		var temp = Files.createTempFile("optic_lua_", ".java");
		var options = new Options();
		options.disable(KEEP_COMMENTS);
		options.disable(DEBUG_COMMENTS);
		options.enable(PARALLEL);
		options.enable(VERIFY);
		options.enable(SSA_SPLIT);
		options.enable(LOOP_SPLIT);
		options.set(INDENT, "\t");
		var pipeline = new Pipeline(
				options,
				new LogMessageReporter(log, new SimpleMessageFormat()),
				codeSource
		);
		pipeline.registerPlugin(SingleAssignmentVerifier::new);
		pipeline.registerPlugin(SingleRegisterUseVerifier::new);
		pipeline.registerPlugin(JavaCodeOutput.writingTo(Files.newOutputStream(temp)));
		try {
			pipeline.run();
		} catch (CompilationFailure e) {
			System.err.print("Failed!");
			e.printStackTrace();
			System.exit(1);
		}
		Files.copy(temp, System.err);
		new Compiler(new LogMessageReporter(log, new SimpleMessageFormat())).run(Files.newInputStream(temp), 20);
	}
}