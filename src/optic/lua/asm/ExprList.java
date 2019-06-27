package optic.lua.asm;

import optic.lua.GlobalStats;
import optic.lua.optimization.StaticType;

import java.util.*;

public final class ExprList implements ListNode {
	private static final optic.lua.asm.ExprList EMPTY = new optic.lua.asm.ExprList(List.of(), Optional.empty());

	private final List<ExprNode> nodes;
	private final Optional<ListNode> trailing;

	private ExprList(List<ExprNode> nodes, Optional<ListNode> trailing) {
		this.nodes = nodes;
		this.trailing = trailing;
		GlobalStats.nodesCreated++;
	}

	/**
	 * Returns an empty expression list
	 */
	public static ExprList exprList() {
		return EMPTY;
	}

	/**
	 * Returns an expression list with the given nodes
	 */
	public static ExprList exprList(ListNode... nodes) {
		return exprList(Arrays.asList(nodes));
	}

	/**
	 * Returns an expression list containing a single node
	 */
	public static ExprList exprList(ListNode node) {
		return node.isVararg()
				? new ExprList(List.of(), Optional.of(node))
				: new ExprList(List.of((ExprNode) node), Optional.empty());
	}

	/**
	 * Returns an expression list containing two nodes
	 */
	public static ExprList exprList(ListNode node_1, ListNode node_2) {
		if (node_1.isVararg())
			node_1 = ExprNode.firstOnly(node_1);
		return node_2.isVararg()
				? new ExprList(List.of((ExprNode) node_1), Optional.of(node_2))
				: new ExprList(List.of((ExprNode) node_1, (ExprNode) node_2), Optional.empty());
	}

	/**
	 * Returns an expression list containing the given nodes
	 */
	public static ExprList exprList(List<ListNode> expressions) {
		int size = expressions.size();
		if (size == 0) {
			return EMPTY;
		}
		var copy = new ArrayList<ExprNode>(size);
		for (int i = 0; i < size - 1; i++) {
			copy.add(ExprNode.firstOnly(expressions.get(i)));
		}
		ListNode last = expressions.get(size - 1);
		if (last.isVararg())
			return new ExprList(copy, Optional.of(last));
		copy.add((ExprNode) last);
		return new ExprList(copy, Optional.empty());
	}

	@Override
	public <T, X extends Throwable> T accept(ExpressionVisitor<T, X> visitor) throws X {
		return visitor.visitExprList(nodes, trailing);
	}

	@Override
	public StaticType childTypeInfo(int i) {
		if (nodes.size() > i) {
			return nodes.get(i).typeInfo();
		}
		if (trailing.isPresent()) {
			int offset = i - nodes.size();
			return trailing.get().childTypeInfo(offset);
		}
		return StaticType.OBJECT; // nil
	}

	@Override
	public boolean isPure() {
		for (ExprNode node : nodes)
			if (!node.isPure())
				return false;
		return trailing.isEmpty() || trailing.get().isPure();
	}

	public ExprNode getLeading(int i) {
		return nodes.get(i);
	}

	Optional<ListNode> getTrailing() {
		return trailing;
	}

	int expressionCount() {
		return nodes.size() + (trailing.isPresent() ? 1 : 0);
	}

	@Override
	public String toString() {
		var joiner = new StringJoiner(", ");
		nodes.forEach(node -> joiner.add(node.toString()));
		trailing.ifPresent(node -> joiner.add(node.toString()));
		return joiner.toString();
	}

	public List<ExprNode> getLeading() {
		return nodes;
	}
}
