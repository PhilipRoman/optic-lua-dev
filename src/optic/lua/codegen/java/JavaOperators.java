package optic.lua.codegen.java;

import optic.lua.optimization.*;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static optic.lua.optimization.ProvenType.*;

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
				return b == NUMBER;
			case ADD:
			case SUB:
			case MUL:
			case DIV:
			case IDIV:
			case POW:
			case MOD:
				return a == NUMBER && b == NUMBER;
			case BNOT:
				return b == NUMBER;
			case BAND:
			case BOR:
			case BXOR:
			case SHL:
			case SHR:
			case EQ:
			case LE:
			case LT:
				return a == NUMBER || b == NUMBER;
			default:
				throw new IllegalArgumentException(operator.name());
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
