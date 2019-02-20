package optic.lua.util;

import nl.bigo.luaparser.Lua52Walker;
import org.antlr.runtime.tree.*;
import org.jetbrains.annotations.*;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public final class Trees {
	public static final Field[] LUA_PARSER_FIELDS = Lua52Walker.class.getFields();

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

	@Contract("null -> fail; _ -> param1")
	public static Tree expectBinaryOp(Tree tree) {
		checkNull(tree);
		if (!Operators.isBinary(tree)) {
			throw new AssertionError();
		}
		return tree;
	}

	@Contract("null -> fail; _ -> param1")
	public static Tree expectUnaryOp(Tree tree) {
		checkNull(tree);
		if (!Operators.isUnary(tree)) {
			throw new AssertionError();
		}
		return tree;
	}

	@NotNull
	@Contract("null -> fail; !null -> _")
	public static List<?> childrenOf(Tree t) {
		checkNull(t);
		return Objects.requireNonNullElse(((CommonTree) t).getChildren(), List.of());
	}

	public static int[] getChildTypes(Tree t) {
		int[] types = new int[t.getChildCount()];
		for (int i = 0; i < types.length; i++) {
			types[i] = t.getChild(i).getType();
		}
		return types;
	}

	public static int matchRepeated(Tree tree, int[] start, int type, int[] end) {
		if (start.length + end.length > tree.getChildCount()) {
			throw new IllegalArgumentException("Prefix + postfix is longer than array itself");
		}
		int[] actual = getChildTypes(tree);
		if (!Arrays.equals(actual, 0, start.length, start, 0, start.length)) {
			throw new IllegalArgumentException(Arrays.toString(actual) + " doesn't start with " + Arrays.toString(start));
		}
		if (!Arrays.equals(actual, actual.length - end.length, actual.length, end, 0, end.length)) {
			throw new IllegalArgumentException(Arrays.toString(actual) + " doesn't end with " + Arrays.toString(end));
		}
		for (int i = start.length; i < actual.length - end.length; i++) {
			if (actual[i] != type) {
				throw new IllegalArgumentException("Expected " + type + " got " + actual[i]);
			}
		}
		return actual.length - start.length - end.length;
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
				throw new RuntimeException(e);
			}
		}
		return null;
	}

	public static List<String> reverseLookupAll(int[] types) {
		return Arrays.stream(types).mapToObj(Trees::reverseLookupName).map(Objects::toString).collect(Collectors.toList());
	}
}
