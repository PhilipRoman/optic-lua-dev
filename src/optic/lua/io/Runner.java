package optic.lua.io;

import optic.lua.messages.*;
import optic.lua.runtime.LuaContext;
import optic.lua.runtime.invoke.*;
import org.slf4j.*;

import java.lang.reflect.*;
import java.util.List;

public final class Runner {
	private static final Logger log = LoggerFactory.getLogger(Runner.class);

	private final Options options;

	public Runner(Options options) {
		this.options = options;
	}

	public Runner() {
		options = new Options();
	}

	private static void logTimeTaken(long nanos) {
		long millis = nanos / (long) 1e6;
		log.info("Finished in {}ms", millis);
	}

	public Object[] run(Method method, LuaContext luaContext, List<Object> args) {
		final Object[] result;
		long start = System.nanoTime();
		try {
			result = (Object[]) method.invoke(null, new Object[]{luaContext, args.toArray()});
			if (options.get(StandardFlags.SHOW_TIME)) {
				logTimeTaken(System.nanoTime() - start);
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

		if (options.get(StandardFlags.SHOW_RT_STATS)) {
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
}
