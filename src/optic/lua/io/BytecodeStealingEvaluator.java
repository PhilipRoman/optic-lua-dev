package optic.lua.io;

import org.codehaus.commons.compiler.IExpressionEvaluator;
import org.codehaus.janino.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * <p><i>
 * I stole this from <a href="https://github.com/janino-compiler/janino/blob/master/janino/src/test/java/org/codehaus/janino/tests/GithubIssuesTest.java">janino-compiler/janino</a>.
 * </i></p>
 * <p>
 * A "degenerated" {@link ExpressionEvaluator} that suppresses the loading of the generated bytecodes into a
 * class loader. Call {@link #getBytecodes()} after {@link #cook(String)} to get the bytecode of compiled script.
 * <p>
 * {@link ScriptEvaluator}, {@link ClassBodyEvaluator} and {@link SimpleCompiler} should be adaptable in very
 * much the same way.
 * </p>
 * <p>
 * The methods of {@link IExpressionEvaluator} that are related to loaded classes must not be used, and all
 * throw {@link RuntimeException}s.
 * </p>
 *
 * @see #getBytecodes()
 */
final class BytecodeStealingEvaluator extends SimpleCompiler {
	@NotNull
	private final Map<String, byte[]> classes = new HashMap<>();

	/**
	 * @return The bytecodes that were generated when {@link #cook(String)} was invoked
	 */
	Map<String, byte[]> getBytecodes() {
		if (classes.isEmpty())
			throw new IllegalStateException("Must only be called after \"cook()\"");
		return Map.copyOf(classes);
	}

	// Override this method to prevent the loading of the class files into a ClassLoader.
	@Override
	public void cook(Map<String, byte[]> classes) {
		// Instead of loading the bytecodes into a ClassLoader, store the bytecodes in "this.classes".
		this.classes.putAll(classes);
		super.cook(classes);
	}
}
