package optic.lua.files;

import org.codehaus.commons.compiler.CompileException;
import org.codehaus.janino.ScriptEvaluator;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;

public class Compiler {
	public static void run(Path path) {
		var evaluator = new ScriptEvaluator();
		try {
			evaluator.cookFile(path.toAbsolutePath().toString());
			evaluator.evaluate(new Object[0]);
		} catch (CompileException | IOException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
}
