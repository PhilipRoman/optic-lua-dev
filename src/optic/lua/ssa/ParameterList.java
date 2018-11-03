package optic.lua.ssa;

import nl.bigo.luaparser.Lua52Walker;
import optic.lua.util.TreeTypes;
import org.antlr.runtime.tree.*;

import java.util.*;
import java.util.stream.*;

import static nl.bigo.luaparser.Lua52Walker.*;

/**
 * Represents a possibly empty list of named parameters, optionally followed by a vararg (...)
 */
public final class ParameterList {
	private final List<String> names;

	private ParameterList(List<String> names) {
		this.names = names;
	}

	public static ParameterList parse(CommonTree tree) {
		TreeTypes.expect(Lua52Walker.PARAM_LIST, tree);
		var names = Optional.ofNullable(tree.getChildren())
				.orElse(Collections.emptyList())
				.stream()
				.map(Tree.class::cast)
				.peek(x -> TreeTypes.expectEither(DotDotDot, Name, x))
				.map(Object::toString)
				.collect(Collectors.toList());
		return new ParameterList(names);
	}

	public boolean hasVarargs() {
		return !names.isEmpty() && names.get(names.size() - 1).equals("...");
	}

	public OptionalInt indexOf(String parameterName) {
		int index = names.indexOf(parameterName);
		return index > 0 ? OptionalInt.of(index) : OptionalInt.empty();
	}

	@Override
	public String toString() {
		return names.toString();
	}
}
