package optic.lua.messages;

/**
 * Indicates that an internal assumption made by the compiler has been violated. This error usually means that
 * the compiler has a bug. Usually it should not be caught.
 */
public class InternalCompilerError extends Error {
	public InternalCompilerError(String message) {
		super(message);
	}

	public InternalCompilerError(String message, Throwable cause) {
		super(message, cause);
	}

	public InternalCompilerError(Throwable cause) {
		super(cause);
	}
}
