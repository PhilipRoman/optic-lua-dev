package optic.lua.codegen.java;

import optic.lua.util.UniqueNames;

import java.util.*;

public class NestedData {
	/**
	 * <p>
	 * Stack containing names of varargs variables in nested functions.
	 * There is an entry for each level of nested functions. If an Optional
	 * is empty, it means that function did not have a vararg parameter.
	 * </p>
	 * <p>
	 * Initially, the stack contains the root vararg.
	 * </p>
	 */
	private final Deque<Optional<String>> varargNamesInFunction = new ArrayDeque<>(8);

	{
		pushNewVarargName();
	}

	/**
	 * @see #varargNamesInFunction
	 */
	private final Deque<String> contextNamesInFunction = new ArrayDeque<>(8);

	public String pushNewVarargName() {
		String name = "varargs_" + UniqueNames.next();
		varargNamesInFunction.addFirst(Optional.of(name));
		return name;
	}

	public void pushMissingVarargName() {
		varargNamesInFunction.addFirst(Optional.empty());
	}

	public void popLastVarargName() {
		varargNamesInFunction.removeFirst();
	}

	public Optional<String> varargName() {
		return varargNamesInFunction.peekFirst();
	}

	public String pushNewContextName() {
		String name = "context_" + UniqueNames.next();
		contextNamesInFunction.addFirst(name);
		return name;
	}

	public String contextName() {
		return contextNamesInFunction.peekFirst();
	}

	public void popLastContextName() {
		contextNamesInFunction.removeFirst();
	}
}
