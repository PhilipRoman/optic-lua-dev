package optic.lua.codegen.java;

import optic.lua.util.UniqueNames;

import java.util.*;

class NestedData {
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
	/**
	 * @see #varargNamesInFunction
	 */
	private final Deque<String> contextNamesInFunction = new ArrayDeque<>(8);

	{
		pushNewVarargName();
	}

	String pushNewVarargName() {
		String name = "varargs_" + UniqueNames.next();
		varargNamesInFunction.addFirst(Optional.of(name));
		return name;
	}

	void pushMissingVarargName() {
		varargNamesInFunction.addFirst(Optional.empty());
	}

	void popLastVarargName() {
		varargNamesInFunction.removeFirst();
	}

	Optional<String> varargName() {
		return varargNamesInFunction.peekFirst();
	}

	String pushNewContextName() {
		String name = "context_" + UniqueNames.next();
		contextNamesInFunction.addFirst(name);
		return name;
	}

	String contextName() {
		return contextNamesInFunction.peekFirst();
	}

	void popLastContextName() {
		contextNamesInFunction.removeFirst();
	}

	Optional<String> firstNestedVarargName() {
		for (var o : varargNamesInFunction) {
			if (o.isPresent()) {
				return o;
			}
		}
		return Optional.empty();
	}

	String rootContextName() {
		return Objects.requireNonNull(contextNamesInFunction.peekLast());
	}
}
