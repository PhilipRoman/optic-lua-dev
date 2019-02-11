package optic.lua.optimization;

import nl.bigo.luaparser.Lua52Walker;
import optic.lua.asm.instructions.InvocationMethod;
import optic.lua.util.Trees;

import java.util.Objects;

import static nl.bigo.luaparser.Lua52Walker.*;
import static optic.lua.optimization.ProvenType.*;

public enum LuaOperator {
	ADD, SUB, MUL, DIV, IDIV, POW, UNM, MOD, CONCAT, BAND, BOR, BXOR, BNOT, SHL, SHR, EQ, LT, LE, LEN;

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

	public ProvenType resultType(ProvenType a, ProvenType b) {
		if (arity() == 2) {
			Objects.requireNonNull(a);
		}
		Objects.requireNonNull(b);
		switch (this) {
			case UNM:
				return b;
			case ADD:
			case SUB:
			case MUL:
			case IDIV:
			case POW:
			case MOD:
				return a.and(b);
			case DIV:
				return a.isNumeric() && b.isNumeric() ? NUMBER : OBJECT;
			case CONCAT:
				return OBJECT;
			case BNOT:
				return b;
			case BAND:
			case BOR:
			case BXOR:
			case SHL:
			case SHR:
				if (a == INTEGER || b == INTEGER) {
					return INTEGER;
				} else {
					return OBJECT;
				}
			case EQ:
			case LT:
			case LE:
				return OBJECT;
			case LEN:
				return OBJECT;
			default:
				throw new IllegalArgumentException(this.name());
		}
	}

	/**
	 * <h1>Important!</h1>
	 * <strong>This method has two special cases when the argument is {@link Lua52Walker#GTEq} and {@link Lua52Walker#GT} where
	 * it will return the inverse operator ({@link #LT} and {@link #LE}) respectively. Make sure to check for these cases
	 * and reverse the arguments if necessary.</strong>
	 */
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
			case LTEq:
			case GT:
				return LE;
			case Lua52Walker.LT:
			case GTEq:
				return LT;
			default:
				throw new IllegalArgumentException(Trees.reverseLookupName(type));
		}
	}

	public InvocationMethod invocationTarget() {
		return InvocationMethod.valueOf(this.name());
	}
}
