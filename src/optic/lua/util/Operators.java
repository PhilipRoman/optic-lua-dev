package optic.lua.util;

import nl.bigo.luaparser.Lua52Walker;
import org.antlr.runtime.tree.Tree;

import java.util.*;

public final class Operators {
	private static final BitSet binaryOperators = new BitSet(128);
	private static final BitSet unaryOperators = new BitSet(128);

	static {
		Set.of(
				Lua52Walker.Add,
				Lua52Walker.Minus,
				Lua52Walker.Div,
				Lua52Walker.FloorDiv,
				Lua52Walker.Mult,
				Lua52Walker.BitAnd,
				Lua52Walker.BitOr,
				Lua52Walker.Tilde,
				Lua52Walker.BitLShift,
				Lua52Walker.BitRShift,
				Lua52Walker.DotDot,
				Lua52Walker.Dot,
				Lua52Walker.Eq,
				Lua52Walker.GTEq,
				Lua52Walker.LTEq,
				Lua52Walker.LT,
				Lua52Walker.GT,
				Lua52Walker.NEq,
				Lua52Walker.Or,
				Lua52Walker.And
		).forEach(binaryOperators::set);
		Set.of(
				Lua52Walker.BIT_NOT,
				Lua52Walker.UNARY_MINUS
		).forEach(unaryOperators::set);
	}

	private Operators() {
	}

	public static boolean isBinary(Tree tree) {
		return binaryOperators.get(tree.getType());
	}

	public static boolean isUnary(Tree tree) {
		return unaryOperators.get(tree.getType());
	}
}
