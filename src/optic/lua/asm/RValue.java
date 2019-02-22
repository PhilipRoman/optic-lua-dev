package optic.lua.asm;

import optic.lua.asm.InvocationMethod.ReturnCount;
import optic.lua.optimization.ProvenType;
import optic.lua.util.Numbers;

import java.util.*;

public interface RValue {
	static RValue varargs() {
		return new Varargs();
	}

	static RValue number(double num) {
		return new NumberConstant(num);
	}

	static RValue string(String s) {
		return new StringConstant(s);
	}

	static RValue bool(boolean b) {
		return new BooleanConstant(b);
	}

	static RValue nil() {
		return new NilConstant();
	}

	static RValue table(LinkedHashMap<RValue, RValue> entries) {
		return new TableLiteral(entries);
	}

	static RValue function(ParameterList parameters, AsmBlock body) {
		return new FunctionLiteral(parameters, body);
	}

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

	static Invocation invocation(RValue obj, InvocationMethod method, List<RValue> arguments) {
		return new Invocation(obj, method, arguments);
	}

	<T, X extends Throwable> T accept(RValueVisitor<T, X> visitor) throws X;

	default boolean isVararg() {
		return false;
	}

	default boolean isPure() {
		return false;
	}

	default FlatExpr discardRemaining() {
		if (this.isVararg()) {
			var r = RegisterFactory.create();
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
			return method.typeInfo(arguments);
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
			Register result = RegisterFactory.create();
			Step step = StepFactory.select(result, this, 0);
			return new FlatExpr(List.of(step), result);
		}
	}
}

