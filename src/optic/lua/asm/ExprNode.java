package optic.lua.asm;

import optic.lua.optimization.ProvenType;
import optic.lua.util.Numbers;
import org.codehaus.janino.InternalCompilerException;

import java.util.LinkedHashMap;

/**
 * A read-only expression. May return more than one value (see {@link #isVararg()}). Care must be taken not to
 * evaluate the same expression more than once unless it is known to be side-effect free (as indicated by
 * {@link #isPure()}).
 * <br>
 * Use static factory methods to create instances of this interface.
 */
public interface ExprNode extends ListNode {

	/**
	 * Returns a node which has the value of the given number constant.
	 */
	static ExprNode number(double num) {
		return new NumberConstant(num);
	}

	/**
	 * Returns a node which has the value of the given string constant.
	 */
	static ExprNode string(String s) {
		return new StringConstant(s);
	}

	/**
	 * Returns a node which has the value of the given boolean constant.
	 */
	static ExprNode bool(boolean b) {
		return new BooleanConstant(b);
	}

	/**
	 * Returns a node which has the value of nil.
	 */
	static ExprNode nil() {
		return new NilConstant();
	}

	/**
	 * Returns a node which describes a table constructor expression from the given entries.
	 */
	static ExprNode table(LinkedHashMap<ExprNode, ListNode> entries) {
		return new TableLiteral(entries);
	}

	/**
	 * Returns a node which describes an anonymous function with given parameter list and body.
	 */
	static ExprNode function(ParameterList parameters, AsmBlock body) {
		return new FunctionLiteral(parameters, body);
	}

	/**
	 * Returns a node which references the given variable.
	 */
	static ExprNode variableName(VariableInfo variableInfo) {
		return new Name(variableInfo);
	}

	/**
	 * Returns a node which references the result of applying an {@link InvocationMethod} with arguments to a value.
	 */
	static Invocation invocation(ExprNode obj, InvocationMethod method, ListNode arguments) {
		return new Invocation(obj, method, arguments);
	}


	/**
	 * Returns a node which references the result of applying an {@link InvocationMethod} with arguments to a value.
	 */
	static MonoInvocation monoInvocation(ExprNode obj, InvocationMethod method, ListNode arguments) {
		return new MonoInvocation(obj, method, arguments);
	}


	/**
	 * Returns a node which describes the result of logical "or" of two expressions.
	 */
	static ExprNode logicalOr(ExprNode a, ExprNode b) {
		return new Logical(false, a, b);
	}

	/**
	 * Returns a node which describes the result of logical "and" of two expressions.
	 */
	static ExprNode logicalAnd(ExprNode a, ExprNode b) {
		return new Logical(true, a, b);
	}

	/**
	 * Returns a node which describes the result of logical "not" of an expression.
	 */
	static ExprNode logicalNot(ExprNode x) {
		return new Not(x);
	}

	/**
	 * Returns a node which references only the first value returned by the given expression.
	 */
	static ExprNode firstOnly(ListNode x) {
		return x.isVararg() ? new Selected(x, 0) : (ExprNode) x;
	}

	/**
	 * Returns a node which references only the n-th value returned by the given expression.
	 */
	static ExprNode selectNth(ListNode x, int n) {
		return x.isVararg() ? new Selected(x, n) : ExprNode.nil();
	}

	@Override
	default ProvenType childTypeInfo(int i) {
		return i == 0 ? typeInfo() : ProvenType.OBJECT;
	}

	/**
	 * Returns true if this expression may return multiple (or zero) values.
	 */
	@Override
	default boolean isVararg() {
		return false;
	}

	/**
	 * Returns true if this expression is guaranteed to have no side effects.
	 */
	@Override
	default boolean isPure() {
		return false;
	}

	default ProvenType typeInfo() {
		return ProvenType.OBJECT;
	}

	final class Selected implements ExprNode {
		private final ListNode source;
		private final int n;

		private Selected(ListNode source, int n) {
			if (!source.isVararg()) {
				throw new InternalCompilerException("Source should be vararg (got: " + source + ")");
			}
			if (n < 0) {
				throw new InternalCompilerException("n should be at least 0 (got: " + n + ")");
			}
			this.n = n;
			this.source = source;
		}

		@Override
		public <T, X extends Throwable> T accept(ExpressionVisitor<T, X> visitor) throws X {
			return visitor.visitSelectNth(source, n);
		}

		@Override
		public boolean isPure() {
			return source.isPure();
		}

		@Override
		public ProvenType typeInfo() {
			return source.childTypeInfo(n);
		}
	}

	final class NumberConstant extends Constant<Double> {
		private NumberConstant(double value) {
			super(value);
		}

		@Override
		public <T, X extends Throwable> T accept(ExpressionVisitor<T, X> visitor) throws X {
			return visitor.visitNumberConstant(value);
		}

		@Override
		public ProvenType typeInfo() {
			return Numbers.isInt(value) ? ProvenType.INTEGER : ProvenType.NUMBER;
		}
	}

	final class StringConstant extends Constant<String> {
		private StringConstant(String value) {
			super(value);
		}

		@Override
		public <T, X extends Throwable> T accept(ExpressionVisitor<T, X> visitor) throws X {
			return visitor.visitStringConstant(value);
		}
	}

	final class BooleanConstant extends Constant<Boolean> {
		private BooleanConstant(boolean value) {
			super(value);
		}

		@Override
		public <T, X extends Throwable> T accept(ExpressionVisitor<T, X> visitor) throws X {
			return visitor.visitBooleanConstant(value);
		}
	}

	final class NilConstant extends Constant<Void> {
		private NilConstant() {
			super(null);
		}

		@Override
		public <T, X extends Throwable> T accept(ExpressionVisitor<T, X> visitor) throws X {
			return visitor.visitNilConstant();
		}
	}

	final class TableLiteral implements ExprNode {
		private final LinkedHashMap<ExprNode, ListNode> entries;

		private TableLiteral(LinkedHashMap<ExprNode, ListNode> entries) {
			this.entries = new LinkedHashMap<>(entries);
		}

		@Override
		public <T, X extends Throwable> T accept(ExpressionVisitor<T, X> visitor) throws X {
			return visitor.visitTableConstructor(this);
		}

		public LinkedHashMap<ExprNode, ListNode> entries() {
			return entries;
		}
	}

	final class FunctionLiteral implements ExprNode {
		private final ParameterList parameters;
		private final AsmBlock body;

		private FunctionLiteral(ParameterList parameters, AsmBlock body) {
			this.parameters = parameters;
			this.body = body;
		}

		@Override
		public <T, X extends Throwable> T accept(ExpressionVisitor<T, X> visitor) throws X {
			return visitor.visitFunctionLiteral(this);
		}

		public AsmBlock body() {
			return body;
		}

		public ParameterList parameters() {
			return parameters;
		}
	}

	final class Name implements ExprNode {
		private final VariableInfo variable;

		private Name(VariableInfo variable) {
			this.variable = variable;
		}

		@Override
		public <T, X extends Throwable> T accept(ExpressionVisitor<T, X> visitor) throws X {
			switch (variable.getMode()) {
				case LOCAL:
					return visitor.visitLocalName(variable);
				case UPVALUE:
					return visitor.visitUpValueName(variable);
				case GLOBAL:
					return visitor.visitGlobalName(variable);
				default:
					throw new AssertionError("Should not reach here");
			}
		}

		@Override
		public ProvenType typeInfo() {
			return variable.typeInfo();
		}

		@Override
		public boolean isPure() {
			return variable.getMode() != VariableMode.GLOBAL;
		}
	}

	/**
	 * An abstract helper class for various kinds of constants.
	 */
	abstract class Constant<T> implements ExprNode {
		protected final T value;

		private Constant(T value) {
			this.value = value;
		}

		public T value() {
			return value;
		}

		@Override
		public boolean isPure() {
			return true;
		}
	}

	final class MonoInvocation extends Invocation implements ExprNode {
		MonoInvocation(ExprNode object, InvocationMethod method, ListNode arguments) {
			super(object, method, arguments);
		}

		@Override
		public ProvenType typeInfo() {
			return method.typeInfo(object, arguments);
		}
	}

	final class Not implements ExprNode {
		private final ExprNode value;

		Not(ExprNode value) {
			if (!value.isPure()) {
				throw new IllegalArgumentException("Argument to 'Not()' must be pure (got " + value + ")");
			}
			this.value = value;
		}

		@Override
		public <T, X extends Throwable> T accept(ExpressionVisitor<T, X> visitor) throws X {
			return visitor.visitNot(value);
		}

		@Override
		public boolean isPure() {
			return true;
		}

		@Override
		public ProvenType typeInfo() {
			return ProvenType.OBJECT;
		}

		@Override
		public String toString() {
			return "Not(" + value + ")";
		}
	}

	/**
	 * Represents "or" and "and" operations. These operations are not implemented as invocations due to lazy evaluation requirements.
	 */
	final class Logical implements ExprNode {
		private final boolean and;
		private final ExprNode first, second;

		Logical(boolean and, ExprNode first, ExprNode second) {
			if (!first.isPure())
				throw new IllegalArgumentException(
						"First operand of logical operator must be pure (got " + first + ")");
			this.and = and;
			this.first = first;
			this.second = second;
		}


		@Override
		public <T, X extends Throwable> T accept(ExpressionVisitor<T, X> visitor) throws X {
			if (and)
				return visitor.visitAnd(first, second);
			else
				return visitor.visitOr(first, second);
		}

		@Override
		public String toString() {
			return "(" + first + (and ? " And " : " Or ") + second + ")";
		}

		@Override
		public boolean isPure() {
			return true;
		}

		@Override
		public ProvenType typeInfo() {
			if (!and && first.typeInfo().isNumeric()) {
				return first.typeInfo();
			}
			if (and && first.typeInfo().isNumeric()) {
				return second.typeInfo();
			}
			return ProvenType.OBJECT;
		}

	}
}

