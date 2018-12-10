package optic.lua.files;

import org.codehaus.commons.compiler.CompileException;
import org.codehaus.janino.ScriptEvaluator;

import java.io.IOException;
import java.nio.file.Path;

public class Compiler {
	public static void run(Path path) {
		var evaluator = new ScriptEvaluator();
		try {
			evaluator.cookFile(path.toAbsolutePath().toString());
		} catch (CompileException | IOException e) {
			throw new RuntimeException(e);
		}
	}
}
