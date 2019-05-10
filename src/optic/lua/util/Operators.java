package optic.lua.util;

import nl.bigo.luaparser.Lua53Walker;
import org.antlr.runtime.tree.Tree;

import java.util.*;

public final class Operators {
	private static final BitSet binaryOperators = new BitSet(128);
	private static final BitSet unaryOperators = new BitSet(128);

	static {
		Set.of(
				Lua53Walker.Add,
				Lua53Walker.Minus,
				Lua53Walker.Div,
				Lua53Walker.FloorDiv,
				Lua53Walker.Mult,
				Lua53Walker.Mod,
				Lua53Walker.Pow,
				Lua53Walker.BitAnd,
				Lua53Walker.BitOr,
				Lua53Walker.Tilde,
				Lua53Walker.BitLShift,
				Lua53Walker.BitRShift,
				Lua53Walker.DotDot,
				Lua53Walker.Dot,
				Lua53Walker.Eq,
				Lua53Walker.GTEq,
				Lua53Walker.LTEq,
				Lua53Walker.LT,
				Lua53Walker.GT,
				Lua53Walker.NEq,
				Lua53Walker.Or,
				Lua53Walker.And
		).forEach(binaryOperators::set);
		Set.of(
				Lua53Walker.BIT_NOT,
				Lua53Walker.UNARY_MINUS,
				Lua53Walker.Length
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
