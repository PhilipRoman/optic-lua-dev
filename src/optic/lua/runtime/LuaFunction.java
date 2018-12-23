package optic.lua.runtime;

import java.util.function.Function;

@RuntimeApi
public abstract class LuaFunction {
	@RuntimeApi
	public LuaFunction() {
	}

	@RuntimeApi
	public abstract Object[] call(Object... args);

	public static LuaFunction of(Function<Object[], Object[]> fun) {
		return new LuaFunction() {
			@Override
			public Object[] call(Object... args) {
				return fun.apply(args);
			}
		};
	}
}
