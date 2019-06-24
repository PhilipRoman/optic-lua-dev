package optic.lua.asm;

import java.util.*;

/**
 * Obtain instances of this interface using {@link StepFactory} methods.
 */
public interface VoidNode extends Node {
	default List<VoidNode> children() {
		return List.of();
	}

	<T, X extends Throwable> T accept(StatementVisitor<T, X> visitor) throws X;

	final class Assign implements VoidNode {
		private final Register result;
		private final ExprNode value;

		Assign(Register result, ExprNode value) {
			this.result = result;
			this.value = value;
			if (!value.typeInfo().subtypeOf(result.typeInfo())) {
				throw new IllegalArgumentException(value + " not assignable to " + result);
			}
		}

		@Override
		public <T, X extends Throwable> T accept(StatementVisitor<T, X> visitor) throws X {
			return visitor.visitAssignment(result, value);
		}
	}

	final class Block implements VoidNode {
		private final AsmBlock steps;

		Block(AsmBlock steps) {
			this.steps = steps;
		}

		@Override
		public <T, X extends Throwable> T accept(StatementVisitor<T, X> visitor) throws X {
			return visitor.visitBlock(steps);
		}
	}

	final class BreakIf implements VoidNode {
		private final ExprNode condition;
		private final boolean isTrue;

		BreakIf(ExprNode condition, boolean isTrue) {
			this.condition = condition;
			this.isTrue = isTrue;
		}

		@Override
		public <T, X extends Throwable> T accept(StatementVisitor<T, X> visitor) throws X {
			return visitor.visitBreakIf(condition, isTrue);
		}
	}

	final class Comment implements VoidNode {
		private final String text;

		Comment(String text) {
			this.text = text;
		}

		@Override
		public <T, X extends Throwable> T accept(StatementVisitor<T, X> visitor) throws X {
			return visitor.visitComment(text);
		}
	}

	final class Declare implements VoidNode {
		private final VariableInfo variable;

		Declare(VariableInfo variable) {
			this.variable = variable;
		}

		@Override
		public <T, X extends Throwable> T accept(StatementVisitor<T, X> visitor) throws X {
			return visitor.visitDeclaration(variable);
		}
	}

	final class ForEachLoop implements VoidNode {
		private final List<VariableInfo> variables;
		private final ExprNode iterator;
		private final AsmBlock body;

		ForEachLoop(List<VariableInfo> variables, ExprNode iterator, AsmBlock body) {
			this.variables = variables;
			this.iterator = iterator;
			this.body = body;
		}

		@Override
		public <T, X extends Throwable> T accept(StatementVisitor<T, X> visitor) throws X {
			return visitor.visitForEachLoop(variables, iterator, body);
		}
	}

	final class ForRangeLoop implements VoidNode {
		private final VariableInfo counter;
		private final ExprNode from;
		private final ExprNode to;
		private final ExprNode step;
		private final AsmBlock block;

		ForRangeLoop(VariableInfo counter, ExprNode from, ExprNode to, ExprNode step, AsmBlock block) {
			this.counter = counter;
			this.from = from;
			this.to = to;
			this.step = step;
			this.block = block;
		}

		@Override
		public <T, X extends Throwable> T accept(StatementVisitor<T, X> visitor) throws X {
			return visitor.visitForRangeLoop(counter, from, to, step, block);
		}
	}

	final class IfElseChain implements VoidNode {
		private final LinkedHashMap<FlatExpr, AsmBlock> clauses;

		IfElseChain(LinkedHashMap<FlatExpr, AsmBlock> clauses) {
			this.clauses = clauses;
		}

		@Override
		public <T, X extends Throwable> T accept(StatementVisitor<T, X> visitor) throws X {
			return visitor.visitIfElseChain(clauses);
		}
	}

	final class Loop implements VoidNode {
		private final AsmBlock body;

		Loop(AsmBlock body) {
			this.body = body;
		}

		@Override
		public <T, X extends Throwable> T accept(StatementVisitor<T, X> visitor) throws X {
			return visitor.visitLoop(body);
		}
	}

	final class Return implements VoidNode {
		private final ListNode values;

		Return(ListNode values) {
			this.values = values;
		}

		@Override
		public <T, X extends Throwable> T accept(StatementVisitor<T, X> visitor) throws X {
			return visitor.visitReturn(values);
		}
	}

	final class Void implements VoidNode {
		private final ListNode value;

		Void(ListNode value) {
			this.value = value;
		}

		@Override
		public <T, X extends Throwable> T accept(StatementVisitor<T, X> visitor) throws X {
			return visitor.visitVoid(value);
		}
	}

	final class Write implements VoidNode {
		final ExprNode source;
		private final VariableInfo target;

		Write(VariableInfo target, ExprNode source) {
			this.target = target;
			this.source = source;
		}

		@Override
		public <T, X extends Throwable> T accept(StatementVisitor<T, X> visitor) throws X {
			return visitor.visitWrite(target, source);
		}
	}

	final class LineNumber implements VoidNode {
		private final int number;

		public LineNumber(int number) {
			if (number <= 0)
				throw new IllegalArgumentException("Line number must be greater than 0");
			this.number = number;
		}

		@Override
		public <T, X extends Throwable> T accept(StatementVisitor<T, X> visitor) throws X {
			return visitor.visitLineNumber(number);
		}
	}

	final class AssignArray implements VoidNode {
		private final ArrayRegister result;
		private final ListNode value;

		public AssignArray(ArrayRegister result, ListNode value) {
			this.result = result;
			this.value = value;
		}

		@Override
		public <T, X extends Throwable> T accept(StatementVisitor<T, X> visitor) throws X {
			return visitor.visitArrayAssignment(result, value);
		}
	}
}
