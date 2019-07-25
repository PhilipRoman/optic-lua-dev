package optic.lua.optimization;

import optic.lua.asm.*;
import optic.lua.asm.ExprNode.*;
import org.jetbrains.annotations.Nullable;

import static optic.lua.optimization.StaticType.*;

public interface Intrinsics {
	Intrinsics DEFAULT = new DefaultIntrinsics();

	static Intrinsics getDefault() {
		return DEFAULT;
	}

	@Nullable
	ExprNode.IntrinsicCall lookup(ExprNode object, InvocationMethod method, ExprList args);

	class DefaultIntrinsics implements Intrinsics {
		@Override
		@Nullable
		public ExprNode.IntrinsicCall lookup(ExprNode object, InvocationMethod method, ExprList args) {
			if (method != InvocationMethod.CALL) {
				return null;
			}
			if (!(object instanceof Name)) {
				return null;
			}
			String name = ((Name) object).accept(new SpecificNodeVisitor<String, RuntimeException>() {
				@Override
				public String visitGlobalName(VariableInfo variable) throws RuntimeException {
					return variable.getName();
				}
			});
			if (name == null) {
				return null;
			}
			switch (name) {
				case "__tonumber":
					return new IntrinsicCall(new FunctionSignature(OBJECT, NUMBER), "toNum", args);
				case "__toint":
					return new IntrinsicCall(new FunctionSignature(OBJECT, INTEGER), "toInt", args);

				case "__bitcount":
					return new IntrinsicCall(new FunctionSignature(INTEGER, INTEGER), "Long.bitCount", args);
				case "__sin":
					return new IntrinsicCall(new FunctionSignature(NUMBER, NUMBER), "Math.sin", args);
				case "__cos":
					return new IntrinsicCall(new FunctionSignature(NUMBER, NUMBER), "Math.cos", args);
				case "__sqrt":
					return new IntrinsicCall(new FunctionSignature(NUMBER, NUMBER), "Math.sqrt", args);
				case "__atan2":
					return new IntrinsicCall(new FunctionSignature(NUMBER, NUMBER), "Math.atan2", args);
				default:
					return null;
			}
		}
	}
}
