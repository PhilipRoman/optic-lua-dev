package optic.lua.runtime;

import java.util.function.Function;

@RuntimeApi
public abstract class DynamicFunction extends Dynamic {
	@RuntimeApi
	public DynamicFunction() {
		super(Dynamic.FUNCTION);
	}

	@RuntimeApi
	public abstract MultiValue call(MultiValue args);

	public static DynamicFunction of(Function<MultiValue, MultiValue> fun) {
		return new DynamicFunction() {
			@Override
			public MultiValue call(MultiValue args) {
				return fun.apply(args);
			}
		};
	}
}
