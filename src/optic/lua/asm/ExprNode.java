package optic.lua.asm;

import optic.lua.GlobalStats;
import optic.lua.optimization.StaticType;
import optic.lua.util.*;
import org.codehaus.janino.InternalCompilerException;
import org.jetbrains.annotations.Contract;
import org.slf4j.*;

import java.util.*;

/**
 * A read-only expression. May return only one value ({@link #isVararg()} must return false). Care must be taken not to
 * evaluate the same expression more than once unless it is known to be side-effect free (as indicated by
 * {@link #isPure()}).
 * <br>
 * Use static factory methods to create instances of this interface.
 */
public interface ExprNode extends ListNode {
	Logger log = LoggerFactory.getLogger(ExprNode.class);

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
	 * The invocation must only yield a single value. Using this function to create multi-valued
	 * invocations is undefined behaviour.
	 */
	static MonoInvocation monoInvocation(ExprNode obj, InvocationMethod method, ExprList arguments) {
		return new MonoInvocation(obj, method, arguments);
	}

	/**
	 * Returns a node which describes the result of logical "or" of two expressions.
	 */
	static ExprNode logicalOr(ExprNode first, ExprNode second) {
		if (alwaysTrue(first)) {
			log.warn("\"{} or {}\" can be simplified to \"{}\"", first, second, first);
			return first;
		}
		if (alwaysFalse(first)) {
			log.warn("\"{} or {}\" can be simplified to \"{}\"", first, second, second);
			return second;
		}
		return new Logical(false, first, second);
	}

	/**
	 * Returns a node which describes the result of logical "and" of two expressions.
	 */
	static ExprNode logicalAnd(ExprNode first, ExprNode second) {
		if (alwaysTrue(first)) {
			log.warn("\"{} and {}\" can be simplified to \"{}\"", first, second, second);
			return second;
		}
		if (alwaysFalse(first)) {
			log.warn("\"{} and {}\" can be simplified to \"{}\"", first, second, first);
			return first;
		}
		return new Logical(true, first, second);
	}

	/**
	 * Returns a node which describes the result of logical "not" of an expression.
	 */
	static ExprNode logicalNot(ExprNode x) {
		if (x.typeInfo() != StaticType.BOOLEAN)
			throw new IllegalArgumentException("expected boolean, got " + x.typeInfo().toString());
		if (alwaysTrue(x)) {
			log.warn("Expression is always false: \"not {}\"", x);
			return ExprNode.bool(false);
		}
		if (alwaysFalse(x)) {
			log.warn("Expression is always true: \"not {}\"", x);
			return ExprNode.bool(true);
		}
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

	/**
	 * Returns a node which references only the value in given table that was associated with the given key
	 */
	static ExprNode tableIndex(ExprNode table, ExprNode key) {
		return monoInvocation(table, InvocationMethod.INDEX, ExprList.exprList(key));
	}

	/**
	 * Returns a node which describes the original value, coerced to number
	 */
	static ExprNode toNumber(ExprNode a) {
		if (a.typeInfo().isNumeric()) {
			return a;
		}
		return monoInvocation(a, InvocationMethod.TO_NUMBER, ExprList.exprList());
	}

	/**
	 * Returns a node which describes the original value, coerced to boolean
	 */
	static ExprNode toBoolean(ExprNode a) {
		if (a.typeInfo() == StaticType.BOOLEAN) {
			return a;
		}
		return monoInvocation(a, InvocationMethod.TO_BOOLEAN, ExprList.exprList());
	}

	@Contract(pure = true)
	static boolean alwaysTrue(ExprNode node) {
		if (node == ExprNode.bool(true))
			return true;
		StaticType type = node.typeInfo();
		return type != StaticType.OBJECT && type != StaticType.BOOLEAN;
	}

	@Contract(pure = true)
	static boolean alwaysFalse(ExprNode node) {
		return node == ExprNode.bool(false) || node == ExprNode.nil();
	}


	@Override
	default StaticType childTypeInfo(int i) {
		return i == 0 ? typeInfo() : StaticType.OBJECT;
	}

	@Override
	default boolean isVararg() {
		return false;
	}

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

		@Override
		public String toString() {
			if (n == 0)
				return source.toString();
			return "select " + n + " " + source.toString();
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

		@Override
		public String toString() {
			if (value.longValue() == value) {
				return Long.toString(value.longValue());
			}
			return Double.toString(value);
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

		@Override
		public String toString() {
			return '"' + StringUtils.escape(value) + '"';
		}

		@Override
		public StaticType typeInfo() {
			return StaticType.STRING;
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

		@Override
		public String toString() {
			return value ? "true" : "false";
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

		@Override
		public String toString() {
			return "nil";
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

		@Override
		public String toString() {
			return "{...}";
		}

		@Override
		public StaticType typeInfo() {
			return StaticType.TABLE;
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

		@Override
		public String toString() {
			return "function(" + String.join(", ", parameters.list()) + ") ... end";
		}

		@Override
		public StaticType typeInfo() {
			return StaticType.FUNCTION;
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

		@Override
		public String toString() {
			return "<" + variable.getMode().toString().toLowerCase() + "> " + variable.getName();
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
		MonoInvocation(ExprNode object, InvocationMethod method, ExprList arguments) {
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
			return "not (" + value + ")";
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
			return "(" + first + (and ? " and " : " or ") + second + ")";
		}

		@Override
		public boolean isPure() {
			return true;
		}

		@Override
		public StaticType typeInfo() {
			// TODO
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

