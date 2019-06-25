package optic.lua.asm;

import optic.lua.GlobalStats;
import optic.lua.asm.InvocationMethod.ReturnCount;
import optic.lua.optimization.StaticType;

import java.util.*;

/**
 * A read-only expression. May return more than one value (see {@link #isVararg()}). Care must be taken not to
 * evaluate the same expression more than once unless it is known to be side-effect free (as indicated by
 * {@link #isPure()}).
 * <br>
 * For single-valued expressions, see {@link ExprNode}
 * <br>
 * Use static factory methods to create instances of this interface.
 */
public interface ListNode extends Node {
	/**
	 * Returns a variable-length RValue which references the varargs ("...") of current scope.
	 */
	static ListNode varargs() {
		return Varargs.VARARGS;
	}

	/**
	 * Returns an empty expression list
	 */
	static ExprList exprList() {
		return ExprList.EMPTY;
	}

	/**
	 * Returns an expression list with the given nodes
	 */
	static ExprList exprList(ListNode... nodes) {
		return exprList(Arrays.asList(nodes));
	}

	/**
	 * Returns an expression list containing a single node
	 */
	static ExprList exprList(ListNode node) {
		return node.isVararg()
				? new ExprList(List.of(), Optional.of(node))
				: new ExprList(List.of((ExprNode) node), Optional.empty());
	}

	/**
	 * Returns an expression list containing two nodes
	 */
	static ExprList exprList(ListNode node_1, ListNode node_2) {
		if (node_1.isVararg())
			node_1 = ExprNode.firstOnly(node_1);
		return node_2.isVararg()
				? new ExprList(List.of((ExprNode) node_1), Optional.of(node_2))
				: new ExprList(List.of((ExprNode) node_1, (ExprNode) node_2), Optional.empty());
	}

	/**
	 * Returns an expression list containing the given nodes
	 */
	static ExprList exprList(List<ListNode> expressions) {
		int size = expressions.size();
		if (size == 0) {
			return ExprList.EMPTY;
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

	/**
	 * Returns a node which references the result of applying an {@link InvocationMethod} with arguments to a value.
	 */
	static Invocation invocation(ExprNode obj, InvocationMethod method, ListNode arguments) {
		return new Invocation(obj, method, arguments);
	}

	/**
	 * Returns the result of applying the given function to this object
	 *
	 * @throws X the exception defined by the function
	 */
	<T, X extends Throwable> T accept(ExpressionVisitor<T, X> visitor) throws X;

	/**
	 * Returns the static type of the n-th value returned by this node
	 */
	StaticType childTypeInfo(int i);

	/**
	 * Returns true if this expression is guaranteed to have no side effects.
	 */
	boolean isPure();

	/**
	 * Returns true if this expression may return multiple (or zero) values.
	 */
	default boolean isVararg() {
		return !(this instanceof ExprNode);
	}


	final class Varargs implements ListNode {
		private static final Varargs VARARGS = new Varargs();

		public <T, X extends Throwable> T accept(ExpressionVisitor<T, X> visitor) throws X {
			return visitor.visitVarargs();
		}

		@Override
		public boolean isPure() {
			return true;
		}

		@Override
		public StaticType childTypeInfo(int i) {
			return StaticType.OBJECT;
		}

		private Varargs() {
			GlobalStats.nodesCreated++;
		}
	}

	final class ExprList implements ListNode {
		private static final ExprList EMPTY = new ExprList(List.of(), Optional.empty());

		private final List<ExprNode> nodes;
		private final Optional<ListNode> trailing;

		private ExprList(List<ExprNode> nodes, Optional<ListNode> trailing) {
			this.nodes = nodes;
			this.trailing = trailing;
			GlobalStats.nodesCreated++;
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

		public List<ExprNode> getLeading() {
			return nodes;
		}

		public Optional<ListNode> getTrailing() {
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
	}

	class Invocation implements ListNode {
		protected final ExprNode object;
		protected final InvocationMethod method;
		protected final ListNode arguments;

		Invocation(ExprNode object, InvocationMethod method, ListNode arguments) {
			this.object = object;
			this.method = method;
			this.arguments = arguments;
			GlobalStats.nodesCreated++;
		}

		@Override
		public <T, X extends Throwable> T accept(ExpressionVisitor<T, X> visitor) throws X {
			return visitor.visitInvocation(this);
		}

		@Override
		public StaticType childTypeInfo(int i) {
			return i == 0 ? method.typeInfo(object, arguments) : StaticType.OBJECT;
		}

		@Override
		public boolean isPure() {
			return false;
		}

		public ExprNode getObject() {
			return object;
		}

		public InvocationMethod getMethod() {
			return method;
		}

		public ListNode getArguments() {
			return arguments;
		}

		@Override
		public boolean isVararg() {
			return method.getReturnCount() == ReturnCount.ANY;
		}

		@Override
		public String toString() {
			switch (method) {
				case INDEX:
					return object + "[" + ((ExprList) arguments).getLeading(0) + "]";
				case CALL:
					return object + "(" + arguments + ")";
				case UNM:
					return "-" + object;
				case BNOT:
					return "~" + object;
				case TO_BOOLEAN:
					return "bool(" + object + ")";
				case TO_NUMBER:
					return "number(" + object + ")";
				default:
					return getMethod().toString().toLowerCase() + "(" + object + ", " + ((ExprList) arguments).getLeading(0) + ")";
			}
		}
	}
}
