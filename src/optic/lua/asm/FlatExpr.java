package optic.lua.asm;

import org.jetbrains.annotations.Contract;

import java.util.List;

/**
 * An immutable container for a list of {@link #block() steps} and
 * a {@link #value() result} which depends on these steps.
 */
public final class FlatExpr {
	private final List<VoidNode> block;
	private final ListNode value;

	public FlatExpr(List<VoidNode> block, ListNode value) {
		this.block = List.copyOf(block);
		this.value = value;
	}

	public ListNode value() {
		return value;
	}

	public List<VoidNode> block() {
		return block;
	}

	/**
	 * A helper method for enabling fluent writing.
	 *
	 * @param list the list to which to append the steps
	 * @return the value of this expression
	 */
	@Contract(mutates = "param1")
	ListNode applyTo(List<VoidNode> list) {
		list.addAll(block);
		return value;
	}

	/**
	 * Returns an expression which returns the same value as this expression,
	 * except that the result will be limited to a single value.
	 */
	FlatExpr discardRemaining() {
		return new FlatExpr(block, ExprNode.firstOnly(value));
	}
}
