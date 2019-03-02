package optic.lua.codegen.java;

import optic.lua.optimization.*;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

final class JavaOperators {
	private JavaOperators() {
	}

	static boolean canApplyJavaSymbol(LuaOperator operator, ProvenType a, ProvenType b) {
		// "a" should be null if operator is unary
		if (operator.arity() == 2) {
			Objects.requireNonNull(a);
		}
		Objects.requireNonNull(b);
		switch (operator) {
			case UNM:
				return b.isNumeric();
			case ADD:
			case SUB:
			case MUL:
			case DIV:
			case IDIV:
			case MOD:
			case EQ:
			case LE:
			case LT:
			case GE:
			case GT:
				return a.isNumeric() && b.isNumeric();
			case BNOT:
				return b == ProvenType.INTEGER;
			case BAND:
			case BOR:
			case BXOR:
			case SHL:
			case SHR:
				return a == ProvenType.INTEGER && b == ProvenType.INTEGER;
			default:
				return false;
		}
	}

	@Nullable
	static String javaSymbol(LuaOperator operator) {
		switch (operator) {
			case ADD:
				return "+";
			case SUB:
			case UNM:
				return "-";
			case MUL:
				return "*";
			case DIV:
				return "/";
			case MOD:
				return "%";
			case BAND:
				return "&";
			case BOR:
				return "|";
			case BXOR:
				return "^";
			case BNOT:
				return "~";
			case SHL:
				return "<<";
			case SHR:
				return ">>";
			case EQ:
				return "==";
			case LT:
				return "<";
			case LE:
				return "<=";
			case GT:
				return ">";
			case GE:
				return ">=";
			default:
				return null;
		}
	}
}
