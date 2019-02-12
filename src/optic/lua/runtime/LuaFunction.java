package optic.lua.runtime;

import java.util.function.*;

@RuntimeApi
public abstract class LuaFunction {
	@RuntimeApi
	public LuaFunction() {
	}

	@RuntimeApi
	public abstract Object[] call(LuaContext context, Object... args);

	public static LuaFunction of(Function<Object[], Object[]> fun) {
		return new LuaFunction() {
			@Override
			public Object[] call(LuaContext context, Object... args) {
				return fun.apply(args);
			}
		};
	}

	public static LuaFunction of(BiFunction<LuaContext, Object[], Object[]> fun) {
		return new LuaFunction() {
			@Override
			public Object[] call(LuaContext context, Object... args) {
				return fun.apply(context, args);
			}
		};
	}

	@Override
	public String toString() {
		return "function 0x" + Integer.toHexString(hashCode());
	}
}
