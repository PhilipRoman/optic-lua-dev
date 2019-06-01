package optic.lua.asm;

import optic.lua.asm.RValue.Invocation;

import java.util.*;

/**
 * Obtain instances of this interface using {@link StepFactory} methods.
 */
public interface Step {
	default List<Step> children() {
		return List.of();
	}

	<T, X extends Throwable> T accept(StepVisitor<T, X> visitor) throws X;

	final class Assign implements Step {
		private final Register result;
		private final RValue value;

		Assign(Register result, RValue value) {
			this.result = result;
			this.value = value;
			if (!value.typeInfo().subtypeOf(result.typeInfo())) {
				throw new IllegalArgumentException(value + " not assignable to " + result);
			}
		}

		@Override
		public <T, X extends Throwable> T accept(StepVisitor<T, X> visitor) throws X {
			return visitor.visitAssignment(result, value);
		}
	}

	final class Block implements Step {
		private final AsmBlock steps;

		Block(AsmBlock steps) {
			this.steps = steps;
		}

		@Override
		public <T, X extends Throwable> T accept(StepVisitor<T, X> visitor) throws X {
			return visitor.visitBlock(steps);
		}
	}

	final class BreakIf implements Step {
		private final RValue condition;
		private final boolean isTrue;

		BreakIf(RValue condition, boolean isTrue) {
			this.condition = condition;
			this.isTrue = isTrue;
		}

		@Override
		public <T, X extends Throwable> T accept(StepVisitor<T, X> visitor) throws X {
			return visitor.visitBreakIf(condition, isTrue);
		}
	}

	final class Comment implements Step {
		private final String text;

		Comment(String text) {
			this.text = text;
		}

		@Override
		public <T, X extends Throwable> T accept(StepVisitor<T, X> visitor) throws X {
			return visitor.visitComment(text);
		}
	}

	final class Declare implements Step {
		private final VariableInfo variable;

		Declare(VariableInfo variable) {
			this.variable = variable;
		}

		@Override
		public <T, X extends Throwable> T accept(StepVisitor<T, X> visitor) throws X {
			return visitor.visitDeclaration(variable);
		}
	}

	final class ForEachLoop implements Step {
		private final List<VariableInfo> variables;
		private final RValue iterator;
		private final AsmBlock body;

		ForEachLoop(List<VariableInfo> variables, RValue iterator, AsmBlock body) {
			this.variables = variables;
			this.iterator = iterator;
			this.body = body;
		}

		@Override
		public <T, X extends Throwable> T accept(StepVisitor<T, X> visitor) throws X {
			return visitor.visitForEachLoop(variables, iterator, body);
		}
	}

	final class ForRangeLoop implements Step {
		private final VariableInfo counter;
		private final RValue from;
		private final RValue to;
		private final RValue step;
		private final AsmBlock block;

		ForRangeLoop(VariableInfo counter, RValue from, RValue to, RValue step, AsmBlock block) {
			this.counter = counter;
			this.from = from;
			this.to = to;
			this.step = step;
			this.block = block;
		}

		@Override
		public <T, X extends Throwable> T accept(StepVisitor<T, X> visitor) throws X {
			return visitor.visitForRangeLoop(counter, from, to, step, block);
		}
	}

	final class IfElseChain implements Step {
		private final LinkedHashMap<FlatExpr, AsmBlock> clauses;

		IfElseChain(LinkedHashMap<FlatExpr, AsmBlock> clauses) {
			this.clauses = clauses;
		}

		@Override
		public <T, X extends Throwable> T accept(StepVisitor<T, X> visitor) throws X {
			return visitor.visitIfElseChain(clauses);
		}
	}

	final class Loop implements Step {
		private final AsmBlock body;

		Loop(AsmBlock body) {
			this.body = body;
		}

		@Override
		public <T, X extends Throwable> T accept(StepVisitor<T, X> visitor) throws X {
			return visitor.visitLoop(body);
		}
	}

	final class Return implements Step {
		private final List<RValue> values;

		Return(List<RValue> values) {
			this.values = values;
		}

		@Override
		public <T, X extends Throwable> T accept(StepVisitor<T, X> visitor) throws X {
			return visitor.visitReturn(values);
		}
	}

	final class Select implements Step {
		private final Register out;
		private final RValue varargs;
		private final int n;

		Select(Register out, RValue varargs, int n) {
			this.out = out;
			this.varargs = varargs;
			this.n = n;
		}

		@Override
		public <T, X extends Throwable> T accept(StepVisitor<T, X> visitor) throws X {
			return visitor.visitSelect(out, n, varargs);
		}
	}

	final class Void implements Step {
		private final RValue.Invocation invocation;

		Void(Invocation invocation) {
			this.invocation = invocation;
		}

		@Override
		public <T, X extends Throwable> T accept(StepVisitor<T, X> visitor) throws X {
			return visitor.visitVoid(invocation);
		}
	}

	final class Write implements Step {
		final RValue source;
		private final VariableInfo target;

		Write(VariableInfo target, RValue source) {
			this.target = target;
			this.source = source;
		}

		@Override
		public <T, X extends Throwable> T accept(StepVisitor<T, X> visitor) throws X {
			return visitor.visitWrite(target, source);
		}
	}

	final class LineNumber implements Step {
		private final int number;

		public LineNumber(int number) {
			if (number <= 0)
				throw new IllegalArgumentException("Line number must be greater than 0");
			this.number = number;
		}

		@Override
		public <T, X extends Throwable> T accept(StepVisitor<T, X> visitor) throws X {
			return visitor.visitLineNumber(number);
		}
	}
}
