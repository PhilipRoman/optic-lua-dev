package optic.lua.util;

import org.antlr.runtime.tree.Tree;
import org.jetbrains.annotations.*;

import static java.lang.String.format;

public final class TreeTypes {
	private TreeTypes() {
	}

	@NotNull
	@Contract("_,null -> fail; _,_ -> param2")
	public static Tree expect(int type, Tree tree) {
		checkNull(tree);
		if (tree.getType() != type) {
			var msg = format("%s is of type %d, not %d",
					tree.toString(),
					tree.getType(),
					type);
			throw new AssertionError(msg);
		}
		return tree;
	}

	@NotNull
	@Contract("_,_,null -> fail; _,_,_ -> param3")
	public static Tree expectEither(int type1, int type2, Tree tree) {
		checkNull(tree);
		if (tree.getType() != type1 && tree.getType() != type2) {
			var msg = format("%s is of type %d, not %d or %d",
					tree.toString(),
					tree.getType(),
					type1,
					type2);
			throw new AssertionError(msg);
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
}
