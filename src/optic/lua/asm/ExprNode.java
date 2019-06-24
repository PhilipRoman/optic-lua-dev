package optic.lua.asm;

import optic.lua.GlobalStats;
import optic.lua.optimization.StaticType;
import optic.lua.util.Numbers;
import org.codehaus.janino.InternalCompilerException;

import java.util.*;

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
		if ((long) num != num)
			return new NumberConstant(num);
		var cached = NumberConstant.CACHE.get((long) num);
		if (cached != null) {
			GlobalStats.nodesReused++;
			return cached;
		}
		var newObj = new NumberConstant(num);
		NumberConstant.CACHE.put((long) num, newObj);
		return newObj;
	}

	/**
	 * Returns a node which has the value of the given string constant.
	 */
	static ExprNode string(String s) {
		var cached = StringConstant.CACHE.get(s);
		if (cached != null) {
			GlobalStats.nodesReused++;
			return cached;
		}
		var newObj = new StringConstant(s);
		StringConstant.CACHE.put(s, newObj);
		return newObj;
	}

	/**
	 * Returns a node which has the value of the given boolean constant.
	 */
	static ExprNode bool(boolean b) {
		GlobalStats.nodesReused++;
		return b ? BooleanConstant.TRUE : BooleanConstant.FALSE;
	}

	/**
	 * Returns a node which has the value of nil.
	 */
	static ExprNode nil() {
		GlobalStats.nodesReused++;
		return NilConstant.NIL;
	}

	/**
	 * Returns a node which describes a table constructor expression from the given entries.
	 */
	static ExprNode table(LinkedHashMap<ExprNode, ListNode> entries) {
		if (entries.isEmpty()) {
			GlobalStats.nodesReused++;
			return TableLiteral.EMPTY;
		}
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
		if (variableInfo.getMode() == VariableMode.GLOBAL) {
			var cached = Name.GLOBAL_CACHE.get(variableInfo.getName());
			if (cached != null) {
				GlobalStats.nodesReused++;
				return cached;
			}
			var newObj = new Name(variableInfo);
			Name.GLOBAL_CACHE.put(variableInfo.getName(), newObj);
			return newObj;
		}
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
		if (alwaysTrue(a)) {
			return a;
		}
		return new Logical(false, a, b);
	}

	/**
	 * Returns a node which describes the result of logical "and" of two expressions.
	 */
	static ExprNode logicalAnd(ExprNode a, ExprNode b) {
		if (alwaysTrue(a)) {
			return b;
		}
		return new Logical(true, a, b);
	}

	/**
	 * Returns a node which describes the result of logical "not" of an expression.
	 */
	static ExprNode logicalNot(ExprNode x) {
		if (alwaysTrue(x)) {
			return bool(false);
		}
		if (x.typeInfo() != StaticType.BOOLEAN)
			throw new IllegalArgumentException("expected boolean, got " + x.typeInfo().toString());
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

	static ExprNode tableIndex(ExprNode table, ExprNode key) {
		return ExprNode.monoInvocation(table, InvocationMethod.INDEX, ListNode.exprList(key));
	}

	static ExprNode toNumber(ExprNode a) {
		if (a.typeInfo().isNumeric()) {
			return a;
		}
		return monoInvocation(a, InvocationMethod.TO_NUMBER, ListNode.exprList());
	}

	static ExprNode toBoolean(ExprNode a) {
		if (a.typeInfo() == StaticType.BOOLEAN) {
			return a;
		}
		if (alwaysTrue(a)) {
			return bool(true);
		}
		return monoInvocation(a, InvocationMethod.TO_BOOLEAN, ListNode.exprList());
	}

	private static boolean alwaysTrue(ExprNode node) {
		StaticType type = node.typeInfo();
		return type != StaticType.OBJECT && type != StaticType.BOOLEAN;
	}

	@Override
	default StaticType childTypeInfo(int i) {
		return i == 0 ? typeInfo() : StaticType.OBJECT;
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

	default StaticType typeInfo() {
		return StaticType.OBJECT;
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
			GlobalStats.nodesCreated++;
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
		public StaticType typeInfo() {
			return source.childTypeInfo(n);
		}
	}

	final class NumberConstant extends Constant<Double> {
		private static final HashMap<Long, NumberConstant> CACHE = new HashMap<>(128);

		private NumberConstant(double value) {
			super(value);
		}

		@Override
		public <T, X extends Throwable> T accept(ExpressionVisitor<T, X> visitor) throws X {
			return visitor.visitNumberConstant(value);
		}

		@Override
		public StaticType typeInfo() {
			return Numbers.isInt(value) ? StaticType.INTEGER : StaticType.NUMBER;
		}
	}

	final class StringConstant extends Constant<String> {
		private static final HashMap<String, StringConstant> CACHE = new HashMap<>(128);

		private StringConstant(String value) {
			super(value);
		}

		@Override
		public <T, X extends Throwable> T accept(ExpressionVisitor<T, X> visitor) throws X {
			return visitor.visitStringConstant(value);
		}
	}

	final class BooleanConstant extends Constant<Boolean> {
		private static final BooleanConstant TRUE = new BooleanConstant(true);
		private static final BooleanConstant FALSE = new BooleanConstant(false);

		private BooleanConstant(boolean value) {
			super(value);
		}

		@Override
		public <T, X extends Throwable> T accept(ExpressionVisitor<T, X> visitor) throws X {
			return visitor.visitBooleanConstant(value);
		}

		@Override
		public StaticType typeInfo() {
			return StaticType.BOOLEAN;
		}
	}

	final class NilConstant extends Constant<Void> {
		private static final NilConstant NIL = new NilConstant();

		private NilConstant() {
			super(null);
		}

		@Override
		public <T, X extends Throwable> T accept(ExpressionVisitor<T, X> visitor) throws X {
			return visitor.visitNilConstant();
		}
	}

	final class TableLiteral implements ExprNode {
		private static final TableLiteral EMPTY = new TableLiteral(new LinkedHashMap<>());
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
		private static final HashMap<String, Name> GLOBAL_CACHE = new HashMap<>(64);
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
		public StaticType typeInfo() {
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
			GlobalStats.nodesCreated++;
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
		public StaticType typeInfo() {
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
			GlobalStats.nodesCreated++;
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
		public StaticType typeInfo() {
			return StaticType.OBJECT;
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
			GlobalStats.nodesCreated++;
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
		public StaticType typeInfo() {
			if (!and && first.typeInfo().isNumeric()) {
				return first.typeInfo();
			}
			if (and && first.typeInfo().isNumeric()) {
				return second.typeInfo();
			}
			return StaticType.OBJECT;
		}

	}
}

