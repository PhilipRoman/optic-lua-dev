package optic.lua.messages;

/**
 * Throw this method from anywhere to indicate that compilation can't be continued because of an error
 */
public class CompilationFailure extends Exception {
	public CompilationFailure() {
	}
}
