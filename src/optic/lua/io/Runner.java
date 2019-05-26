package optic.lua.io;

import optic.lua.messages.*;
import optic.lua.runtime.LuaContext;
import optic.lua.runtime.invoke.*;
import org.slf4j.*;

import java.lang.reflect.*;
import java.util.List;

/**
 * Helper class to run Lua code that has been compiled to a method.
 */
public final class Runner {
	private static final Logger log = LoggerFactory.getLogger(Runner.class);

	private final Options options;

	public Runner(Options options) {
		this.options = options;
	}

	private static void logTimeTaken(long nanos) {
		long millis = nanos / (long) 1e6;
		log.info("Finished in {}ms", millis);
	}

	/**
	 * Runs a given method which represents compiled Lua code. Depending on options,
	 * may report user-friendly information through logging or standard streams.
	 *
	 * @param method     The method to run
	 * @param luaContext The Lua context which will be passed to the method
	 * @param args       Other arguments that will be passed
	 * @return List of values returned from the given method
	 */
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
					site.printTo(System.out);
					System.out.println();
				} else {
					skippedSites++;
				}
			}
			System.err.println(skippedSites + " call sites skipped");
		}
		return result;
	}
}
