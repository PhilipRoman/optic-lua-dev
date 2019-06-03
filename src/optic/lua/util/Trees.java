package optic.lua.util;

import nl.bigo.luaparser.Lua53Walker;
import org.antlr.runtime.tree.*;
import org.codehaus.janino.InternalCompilerException;
import org.jetbrains.annotations.*;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public final class Trees {
	private static final Field[] LUA_PARSER_FIELDS = Lua53Walker.class.getFields();

	private Trees() {
	}

	private static AssertionError failExpect(Tree actual, int... expected) {
		return new AssertionError("Expected " + reverseLookupAll(expected) + ", got " + actual.toStringTree());
	}

	@NotNull
	@Contract("_,null -> fail; _,_ -> param2")
	public static Tree expect(int type, Tree tree) {
		checkNull(tree);
		if (tree.getType() != type) {
			throw failExpect(tree, type);
		}
		return tree;
	}

	@NotNull
	@Contract("_,_,null -> fail; _,_,_ -> param3")
	public static Tree expectEither(int type1, int type2, Tree tree) {
		checkNull(tree);
		if (tree.getType() != type1 && tree.getType() != type2) {
			throw failExpect(tree, type1, type2);
		}
		return tree;
	}

	@NotNull
	@Contract("_,null,_ -> fail; _,_,_ -> param2")
	public static Tree expectChild(int type, Tree tree, int childIndex) {
		checkNull(tree);
		return expect(type, tree.getChild(childIndex));
	}

	@Contract("null->fail; !null->_")
	private static void checkNull(Tree tree) {
		if (tree == null) {
			throw new NullPointerException("Tree was null");
		}
	}

	@NotNull
	@Contract("null -> fail; !null -> _")
	public static List<?> childrenOf(Tree t) {
		checkNull(t);
		List<?> obj = ((CommonTree) t).getChildren();
		return (obj != null) ? obj : List.of();
	}

	public static String reverseLookupName(int type) {
		if (type == -1) {
			return "end-of-file";
		}
		for (var field : LUA_PARSER_FIELDS) {
			try {
				if (field.getType() == Integer.TYPE && field.getInt(null) == type) {
					return field.getName().toLowerCase();
				}
			} catch (IllegalAccessException e) {
				throw new InternalCompilerException("Access to field denied", e);
			}
		}
		return null;
	}

	public static List<String> reverseLookupAll(int[] types) {
		return Arrays.stream(types).mapToObj(Trees::reverseLookupName).map(Objects::toString).collect(Collectors.toList());
	}
}
