package optic.lua.runtime;

@RuntimeApi
public final class UpValue {
	@RuntimeApi
	public Object value = null;

	@RuntimeApi
	public static class OfInt {
		public long value = 0;
	}

	@RuntimeApi
	public static class OfNum {
		public double value = 0.0;
	}

	@RuntimeApi
	public static class OfFunction {
		public LuaFunction value = null;
	}

	@RuntimeApi
	public static class OfTable {
		public LuaTable value = null;
	}
}
