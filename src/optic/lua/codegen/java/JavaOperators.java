package optic.lua.codegen.java;

import optic.lua.optimization.*;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

final class JavaOperators {
	private JavaOperators() {
	}

	static boolean canApplyJavaSymbol(LuaOperator operator, ProvenType a, ProvenType b) {
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
			case POW:
			case MOD:
				return a.isNumeric() && b.isNumeric();
			case BNOT:
				return b == ProvenType.INTEGER;
			case BAND:
			case BOR:
			case BXOR:
			case SHL:
			case SHR:
				return a == ProvenType.INTEGER || b == ProvenType.INTEGER;

			case EQ:
			case LE:
			case LT:
				return a.isNumeric() || b.isNumeric();
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
				return "-";
			case MUL:
				return "*";
			case DIV:
				return "/";
			case UNM:
				return "-";
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
			default:
				return null;
		}
	}
}
