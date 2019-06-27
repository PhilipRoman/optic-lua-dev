package optic.lua.asm;

import optic.lua.GlobalStats;
import optic.lua.asm.InvocationMethod.ReturnCount;
import optic.lua.optimization.StaticType;

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
	 * Returns a node which references the result of applying an {@link InvocationMethod} with arguments to a value.
	 */
	static Invocation invocation(ExprNode obj, InvocationMethod method, ExprList arguments) {
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

	class Invocation implements ListNode {
		protected final ExprNode object;
		protected final InvocationMethod method;
		protected final ExprList arguments;

		Invocation(ExprNode object, InvocationMethod method, ExprList arguments) {
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

		public ExprList getArguments() {
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
					return object + "[" + arguments.getLeading(0) + "]";
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
					return getMethod().toString().toLowerCase() + "(" + object + ", " + arguments.getLeading(0) + ")";
			}
		}
	}
}
