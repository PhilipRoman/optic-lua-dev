package optic.lua.io;

import optic.lua.messages.*;
import optic.lua.runtime.LuaContext;
import optic.lua.runtime.invoke.*;

import java.lang.reflect.*;
import java.util.List;

public final class Runner {
	private final Context context;

	public Runner(Context context) {
		this.context = context;
	}

	public Runner() {
		context = new Context(new Options(), new NullMessageReporter());
	}

	public Object[] run(Method method, LuaContext luaContext, List<Object> args) {
		final Object[] result;
		long start = System.nanoTime();
		try {
			result = (Object[]) method.invoke(null, new Object[]{luaContext, args.toArray()});
			if (context.options().get(StandardFlags.SHOW_TIME)) {
				context.reporter().report(tookTime(System.nanoTime() - start));
			}
		} catch (InvocationTargetException e) {
			if (e.getCause() instanceof Error) {
				throw (Error) e.getCause();
			}
			if (e.getCause() instanceof RuntimeException) {
				throw (RuntimeException) e.getCause();
			}
			throw new RuntimeException(e.getCause());
		} catch (IllegalAccessException e) {
			throw new AssertionError(e);
		}

		if (context.options().get(StandardFlags.SHOW_RT_STATS)) {
			// show information about call sites:
			int skippedSites = 0;
			for (CallSite site : luaContext.getCallSites()) {
				int numberOfInvocations = ((InstrumentedCallSite) site).history().values()
						.stream()
						.reduce(0, Integer::sum);
				if (numberOfInvocations > 1) {
					site.printTo(System.err);
					System.err.println();
				} else {
					skippedSites++;
				}
			}
			System.err.println(skippedSites + " call sites skipped");
		}
		return result;
	}

	private static Message tookTime(long nanos) {
		long millis = nanos / (long) 1e6;
		var msg = Message.createInfo("Finished in " + millis + "ms");
		msg.setPhase(Phase.RUNTIME);
		return msg;
	}
}
