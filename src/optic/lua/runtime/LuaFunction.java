package optic.lua.runtime;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.*;

@RuntimeApi
public abstract class LuaFunction {
	@Nullable
	private final String friendlyName;

	@RuntimeApi
	public LuaFunction() {
		friendlyName = null;
	}

	public LuaFunction(String friendlyName) {
		this.friendlyName = friendlyName;
	}

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

	@RuntimeApi
	public abstract Object[] call(LuaContext context, Object... args);

	@Override
	public String toString() {
		if (friendlyName == null) {
			return "function 0x" + Integer.toHexString(hashCode());
		}
		return "function " + friendlyName;
	}

	public boolean sameCreationSite(LuaFunction function) {
		return Objects.equals(function.friendlyName, friendlyName);
	}

	public int creationSiteHash() {
		return Objects.hashCode(friendlyName);
	}
}
