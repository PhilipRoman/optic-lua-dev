package optic.lua.asm;

import nl.bigo.luaparser.Lua53Walker;
import optic.lua.util.Trees;
import org.antlr.runtime.tree.*;

import java.lang.String;
import java.util.*;
import java.util.stream.Collectors;

import static nl.bigo.luaparser.Lua53Walker.*;

/**
 * Represents a possibly empty list of named parameters, optionally followed by a vararg (...)
 */
public final class ParameterList {
	private final List<String> names;

	private ParameterList(List<String> names) {
		this.names = names;
	}

	static ParameterList parse(CommonTree tree) {
		Trees.expect(Lua53Walker.PARAM_LIST, tree);
		var names = Optional.ofNullable(tree.getChildren())
				.orElse(Collections.emptyList())
				.stream()
				.map(Tree.class::cast)
				.peek(x -> Trees.expectEither(DotDotDot, Name, x))
				.map(Object::toString)
				.collect(Collectors.toList());
		return new ParameterList(List.copyOf(names));
	}

	static ParameterList of(List<String> names) {
		return new ParameterList(List.copyOf(names));
	}

	public boolean hasVarargs() {
		return !names.isEmpty() && names.get(names.size() - 1).equals("...");
	}

	public OptionalInt indexOf(String parameterName) {
		int index = names.indexOf(parameterName);
		return index >= 0 ? OptionalInt.of(index) : OptionalInt.empty();
	}

	@Override
	public String toString() {
		return names.toString();
	}

	public List<java.lang.String> list() {
		return List.copyOf(names);
	}
}
