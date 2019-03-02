package optic.lua.runtime;

import optic.lua.asm.InvocationMethod;

import java.io.PrintStream;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.ToIntFunction;
import java.util.stream.*;

public class InvocationSite {
	private static final Map<String, InvocationSite> sites = new HashMap<>();
	private final InvocationMethod method;
	private final Map<HistoryEntry, Integer> history = new HashMap<>();

	public InvocationSite(InvocationMethod method) {
		this.method = method;
	}

	@RuntimeApi
	public static InvocationSite get(String name, InvocationMethod method) {
		InvocationSite initialized = sites.get(name);
		if (initialized != null) {
			return initialized;
		}
		InvocationSite newSite = new InvocationSite(method);
		sites.put(name, newSite);
		return newSite;
	}

	public static void dumpAll(PrintStream err) {
		sites.forEach((k, v) -> {
			err.println(k);
			err.println(v);
		});
	}

	@RuntimeApi
	public void update(Object object, Object... arguments) {
		ResolvedCall call = ResolvedCall.resolve(object, method);
		boolean[] argumentTypes = new boolean[arguments.length];
		for (int i = 0; i < argumentTypes.length; i++) {
			argumentTypes[i] = arguments[i] instanceof Number;
		}
		HistoryEntry historyEntry = new HistoryEntry(call, argumentTypes);
		int oldCount = history.getOrDefault(historyEntry, 0);
		history.put(historyEntry, oldCount + 1);
	}

	public String toString() {
		Comparator<Map.Entry<?, Integer>> mostCommonFirst = Comparator.comparingInt((ToIntFunction<Entry<?, Integer>>) Entry::getValue).reversed();
		return history.entrySet()
				.stream()
				.sorted(mostCommonFirst)
				.map(entry -> "\t" + entry.getValue() + " * " + entry.getKey())
				.collect(Collectors.joining(System.lineSeparator()));
	}

	public static class HistoryEntry {
		private final int hash;
		private final ResolvedCall function;
		private final boolean[] argumentTypes;

		public HistoryEntry(ResolvedCall function, boolean[] types) {
			this.function = Objects.requireNonNull(function);
			argumentTypes = Objects.requireNonNull(types);
			hash = function.hashCode() ^ Arrays.hashCode(argumentTypes);
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof HistoryEntry) {
				HistoryEntry entry = (HistoryEntry) obj;
				return function.equals(entry.function) && Arrays.equals(this.argumentTypes, entry.argumentTypes);
			}
			return false;
		}

		@Override
		public int hashCode() {
			return hash;
		}

		@Override
		public String toString() {
			return function.toString() + " " + IntStream.range(0, argumentTypes.length)
					.mapToObj(i -> argumentTypes[i] ? "number" : "object")
					.collect(Collectors.joining(", ", "[", "]"));
		}
	}

	public interface ResolvedCall {
		boolean equals(Object o);

		int hashCode();

		static ResolvedCall resolve(Object object, InvocationMethod method) {
			if (object instanceof LuaFunction && method == InvocationMethod.CALL) {
				return new ResolvedFunctionCall((LuaFunction) object);
			}
			throw new UnsupportedOperationException(StandardLibrary.toString(object) + "::" + method.name());
		}
	}

	private static class ResolvedFunctionCall implements ResolvedCall {
		private final LuaFunction function;

		ResolvedFunctionCall(LuaFunction function) {
			this.function = function;
		}

		@Override
		public int hashCode() {
			return function.creationSiteHash();
		}

		@Override
		public boolean equals(Object obj) {
			return obj instanceof ResolvedFunctionCall
					&& ((ResolvedFunctionCall) obj).function.sameCreationSite(function);
		}

		@Override
		public String toString() {
			return function.toString();
		}
	}
}
