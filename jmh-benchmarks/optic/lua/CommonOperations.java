package optic.lua;

import optic.lua.runtime.*;
import org.openjdk.jmh.Main;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.RunnerException;

import java.io.IOException;

@Warmup(iterations = 5, time = 2)
@Fork(1)
@Measurement(iterations = 3, time = 3)
public class CommonOperations {
	private static final LuaTable TABLE = TableOps.create("foo", "bar");
	private static final LuaContext CONTEXT = LuaContext.create();
	private static final LuaFunction FUNCTION = new LuaFunction() {
		@Override
		public Object[] call(LuaContext context, Object... args) {
			return ListOps.create(DynamicOps.add(context, ListOps.get(args, 0), ListOps.get(args, 1)));
		}
	};

	@Benchmark
	public static void setTable() {
		TableOps.setIndex(TABLE, "foo", "bar");
	}

	@Benchmark
	public static Object getTable() {
		return TableOps.index(TABLE, "foo");
	}

	@Benchmark
	public static void empty() {
	}

	@SuppressWarnings("ConstantConditions")
	@Benchmark
	public static double functionCall() {
		return StandardLibrary.toNumber(ListOps.get(FunctionOps.call(FUNCTION, CONTEXT, 2, 3), 0));
	}

	public static void main(String[] args) throws IOException, RunnerException {
		Main.main(args);
	}
}
