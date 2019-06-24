package optic.lua.optimization;

import nl.bigo.luaparser.Lua53Walker;
import optic.lua.asm.InvocationMethod;
import optic.lua.util.Trees;

import java.util.Objects;

import static nl.bigo.luaparser.Lua53Walker.*;
import static optic.lua.optimization.StaticType.*;

public enum LuaOperator {
	ADD, SUB, MUL, DIV, IDIV, POW, UNM, MOD, CONCAT, BAND, BOR, BXOR, BNOT, SHL, SHR, EQ, LT, LE, GT, GE, LEN;

	public static LuaOperator forTokenType(int type) {
		switch (type) {
			case UNARY_MINUS:
				return UNM;
			case Add:
				return ADD;
			case Minus:
				return SUB;
			case Mult:
				return MUL;
			case Div:
				return DIV;
			case Pow:
				return POW;
			case Mod:
				return MOD;
			case FloorDiv:
				return IDIV;
			case DotDot:
				return CONCAT;
			case BitOr:
				return BOR;
			case BitAnd:
				return BAND;
			case BIT_NOT:
				return BNOT;
			case Tilde:
				return BXOR;
			case BitLShift:
				return SHL;
			case BitRShift:
				return SHR;
			case Eq:
				return EQ;
			case Length:
				return LEN;
			case Lua53Walker.LT:
				return LT;
			case GTEq:
				return GE;
			case LTEq:
				return LE;
			case Lua53Walker.GT:
				return GT;
			default:
				throw new IllegalArgumentException(Trees.reverseLookupName(type));
		}
	}

	public int arity() {
		switch (this) {
			case UNM:
			case BNOT:
			case LEN:
				return 1;
			default:
				return 2;
		}
	}

	/**
	 * Returns the most specific possible type of the result of applying values of given types to this operator.
	 * If this operator is unary, the first argument should be null.
	 */
	public StaticType resultType(StaticType a, StaticType b) {
		if (arity() == 2) {
			Objects.requireNonNull(a);
		}
		Objects.requireNonNull(b);
		switch (this) {
			case UNM:
				return b;
			case BNOT:
				return b == OBJECT ? OBJECT : INTEGER;
			case ADD:
			case SUB:
			case MUL:
			case IDIV:
			case MOD:
				return a.and(b);
			case DIV:
				return a.isNumeric() && b.isNumeric() ? NUMBER : OBJECT;
			case CONCAT:
			case EQ:
			case LT:
			case LE:
			case GT:
			case GE:
			case LEN:
				return OBJECT;
			case BAND:
			case BOR:
			case BXOR:
			case SHL:
			case SHR:
				if (a.isNumeric() && b.isNumeric())
					return INTEGER;
				else
					return OBJECT;
			case POW:
				// semantics for POW require double precision even if both operands are natural numbers
				// this is due to large number overflow mechanics
				return NUMBER;
			default:
				throw new IllegalArgumentException(this.name());
		}
	}

	public InvocationMethod invocationMethod() {
		return InvocationMethod.valueOf(this.name());
	}
}
