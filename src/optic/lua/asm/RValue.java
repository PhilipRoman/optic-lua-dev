package optic.lua.asm;

import optic.lua.asm.InvocationMethod.ReturnCount;
import optic.lua.optimization.ProvenType;
import optic.lua.util.Numbers;

import java.util.*;

/**
 * A read-only expression. May return more than one value (see {@link #isVararg()}). Care must be taken not to
 * evaluate the same expression more than once unless it is known to be side-effect free (as indicated by
 * {@link #isPure()}).
 * <br>
 * Use static factory methods to create instances of this interface.
 */
public interface RValue {
	/**
	 * Returns a variable-length RValue which references the varargs ("...") of current function.
	 */
	static RValue varargs() {
		return new Varargs();
	}

	/**
	 * Returns an RValue which has the value of the given number constant.
	 */
	static RValue number(double num) {
		return new NumberConstant(num);
	}

	/**
	 * Returns an RValue which has the value of the given string constant.
	 */
	static RValue string(String s) {
		return new StringConstant(s);
	}

	/**
	 * Returns an RValue which has the value of the given boolean constant.
	 */
	static RValue bool(boolean b) {
		return new BooleanConstant(b);
	}

	/**
	 * Returns an RValue which has the value of nil.
	 */
	static RValue nil() {
		return new NilConstant();
	}

	/**
	 * Returns an RValue which describes a table constructor expression from the given entries.
	 */
	static RValue table(LinkedHashMap<RValue, RValue> entries) {
		return new TableLiteral(entries);
	}

	/**
	 * Returns an RValue which describes an anonymous function with given parameter list and body.
	 */
	static RValue function(ParameterList parameters, AsmBlock body) {
		return new FunctionLiteral(parameters, body);
	}

	/**
	 * Returns an RValue which references the given variable.
	 */
	static RValue variableName(VariableInfo variableInfo) {
		switch (variableInfo.getMode()) {
			case LOCAL:
				return new LocalName(variableInfo);
			case UPVALUE:
				return new UpValueName(variableInfo);
			case GLOBAL:
				return new GlobalName(variableInfo);
		}
		throw new AssertionError("Should never reach here!");
	}

	/**
	 * Returns an RValue which references the result of applying an {@link InvocationMethod} with arguments to a value.
	 */
	static Invocation invocation(RValue obj, InvocationMethod method, List<RValue> arguments) {
		return new Invocation(obj, method, arguments);
	}

	/**
	 * Returns an RValue which describes the result of logical "or" of two expressions.
	 */
	static RValue logicalOr(RValue a, RValue b) {
		return new Logical(false, a, b);
	}

	/**
	 * Returns an RValue which describes the result of logical "and" of two expressions.
	 */
	static RValue logicalAnd(RValue a, RValue b) {
		return new Logical(true, a, b);
	}

	/**
	 * Returns an RValue which describes the result of logical "not" of an expression.
	 */
	static RValue logicalNot(RValue x) {
		return new Not(x);
	}

	<T, X extends Throwable> T accept(RValueVisitor<T, X> visitor) throws X;

	/**
	 * Returns true if this expression may return multiple (or zero) values.
	 */
	default boolean isVararg() {
		return false;
	}

	/**
	 * Returns true if this expression is guaranteed to have no side effects.
	 */
	default boolean isPure() {
		return false;
	}

	/**
	 * Returns an RValue which references the first value returned by this expression.
	 */
	default FlatExpr discardRemaining() {
		if (this.isVararg()) {
			var r = RegisterFactory.create(ProvenType.OBJECT);
			return new FlatExpr(List.of(StepFactory.select(r, this, 0)), r);
		}
		return new FlatExpr(List.of(), this);
	}

	default ProvenType typeInfo() {
		return ProvenType.OBJECT;
	}

	class NumberConstant extends Constant<Double> {
		private NumberConstant(double value) {
			super(value);
		}

		@Override
		public <T, X extends Throwable> T accept(RValueVisitor<T, X> visitor) throws X {
			return visitor.visitNumberConstant(value);
		}

		@Override
		public ProvenType typeInfo() {
			return Numbers.isInt(value) ? ProvenType.INTEGER : ProvenType.NUMBER;
		}
	}

	class StringConstant extends Constant<String> {
		private StringConstant(String value) {
			super(value);
		}

		@Override
		public <T, X extends Throwable> T accept(RValueVisitor<T, X> visitor) throws X {
			return visitor.visitStringConstant(value);
		}
	}

	class BooleanConstant extends Constant<Boolean> {
		private BooleanConstant(boolean value) {
			super(value);
		}

		@Override
		public <T, X extends Throwable> T accept(RValueVisitor<T, X> visitor) throws X {
			return visitor.visitBooleanConstant(value);
		}
	}

	class NilConstant extends Constant<Void> {
		private NilConstant() {
			super(null);
		}

		@Override
		public <T, X extends Throwable> T accept(RValueVisitor<T, X> visitor) throws X {
			return visitor.visitNilConstant();
		}
	}

	class TableLiteral implements RValue {
		private final LinkedHashMap<RValue, RValue> entries;

		private TableLiteral(LinkedHashMap<RValue, RValue> entries) {
			this.entries = new LinkedHashMap<>(entries);
		}

		@Override
		public <T, X extends Throwable> T accept(RValueVisitor<T, X> visitor) throws X {
			return visitor.visitTableConstructor(this);
		}

		public LinkedHashMap<RValue, RValue> entries() {
			return entries;
		}
	}

	class FunctionLiteral implements RValue {
		private final ParameterList parameters;
		private final AsmBlock body;

		private FunctionLiteral(ParameterList parameters, AsmBlock body) {
			this.parameters = parameters;
			this.body = body;
		}

		@Override
		public <T, X extends Throwable> T accept(RValueVisitor<T, X> visitor) throws X {
			return visitor.visitFunctionLiteral(this);
		}

		public AsmBlock body() {
			return body;
		}

		public ParameterList parameters() {
			return parameters;
		}
	}

	class LocalName implements RValue {
		private final VariableInfo variable;

		private LocalName(VariableInfo variable) {
			if (variable.getMode() != VariableMode.LOCAL) {
				throw new IllegalArgumentException(variable.toDebugString() + " is not local!");
			}
			this.variable = variable;
		}

		@Override
		public <T, X extends Throwable> T accept(RValueVisitor<T, X> visitor) throws X {
			return visitor.visitLocalName(variable);
		}

		@Override
		public ProvenType typeInfo() {
			return variable.typeInfo();
		}

		@Override
		public boolean isPure() {
			return true;
		}
	}

	class UpValueName implements RValue {
		private final VariableInfo variable;

		private UpValueName(VariableInfo variable) {
			if (variable.getMode() != VariableMode.UPVALUE) {
				throw new IllegalArgumentException(variable.toDebugString() + " is not an upvalue!");
			}
			this.variable = variable;
		}

		@Override
		public <T, X extends Throwable> T accept(RValueVisitor<T, X> visitor) throws X {
			return visitor.visitUpValueName(variable);
		}
	}

	class GlobalName implements RValue {
		private final VariableInfo variable;

		private GlobalName(VariableInfo variable) {
			if (variable.getMode() != VariableMode.GLOBAL) {
				throw new IllegalArgumentException(variable.toDebugString() + " is not global!");
			}
			this.variable = variable;
		}

		@Override
		public <T, X extends Throwable> T accept(RValueVisitor<T, X> visitor) throws X {
			return visitor.visitGlobalName(variable);
		}
	}

	/**
	 * An abstract helper class for various kinds of constants.
	 */
	abstract class Constant<T> implements RValue {
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

	class Invocation implements RValue {
		private final RValue object;
		private final InvocationMethod method;
		private final List<RValue> arguments;

		Invocation(RValue object, InvocationMethod method, List<RValue> arguments) {
			this.object = object;
			this.method = method;
			this.arguments = arguments;
		}

		@Override
		public <T, X extends Throwable> T accept(RValueVisitor<T, X> visitor) throws X {
			return visitor.visitInvocation(this);
		}

		public RValue getObject() {
			return object;
		}

		public InvocationMethod getMethod() {
			return method;
		}

		public List<RValue> getArguments() {
			return arguments;
		}

		@Override
		public boolean isVararg() {
			return method.getReturnCount() == ReturnCount.ANY;
		}

		@Override
		public ProvenType typeInfo() {
			return method.typeInfo(object, arguments);
		}
	}

	class Varargs implements RValue {
		@Override
		public <T, X extends Throwable> T accept(RValueVisitor<T, X> visitor) throws X {
			return visitor.visitVarargs();
		}

		@Override
		public boolean isVararg() {
			return true;
		}

		@Override
		public FlatExpr discardRemaining() {
			Register result = RegisterFactory.create(ProvenType.OBJECT);
			Step step = StepFactory.select(result, this, 0);
			return new FlatExpr(List.of(step), result);
		}
	}

	class Not implements RValue {
		private final RValue value;

		Not(RValue value) {
			if (!value.isPure()) {
				throw new IllegalArgumentException("Argument to 'Not()' must be a pure RValue (got " + value + ")");
			}
			this.value = value;
		}

		@Override
		public <T, X extends Throwable> T accept(RValueVisitor<T, X> visitor) throws X {
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
	class Logical implements RValue {
		private final boolean and;
		private final RValue first, second;

		Logical(boolean and, RValue first, RValue second) {
			if (!first.isPure())
				throw new IllegalArgumentException(
						"First operand of logical operator must be pure (got " + first + ")");
			this.and = and;
			this.first = first;
			this.second = second;
		}


		@Override
		public <T, X extends Throwable> T accept(RValueVisitor<T, X> visitor) throws X {
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

