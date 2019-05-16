package optic.lua.runtime.invoke;

import optic.lua.runtime.*;

import java.io.PrintStream;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.*;

import static java.util.Comparator.reverseOrder;
import static java.util.Map.Entry.comparingByValue;

public final class InstrumentedCallSite implements CallSite {
	private static final boolean[] EMPTY_BOOL_ARRAY = {};
	private static final boolean[] TRUE_BOOL_ARRAY = {true};
	private static final boolean[] FALSE_BOOL_ARRAY = {false};
	private static final BiFunction<? super Integer, ? super Integer, ? extends Integer> SUM = Integer::sum;
	private final Map<HistoryEntry, Integer> history = new HashMap<>(8);
	private final int id;

	public InstrumentedCallSite(int id) {
		this.id = id;
	}

	@Override
	public Object[] invoke(LuaContext context, Object function, Object[] args) {
		Object[] results = FunctionOps.call(context, function, args);
		var entry = new HistoryEntry(
				((LuaFunction) function).constructionSite(),
				HistoryEntry.encode(args),
				HistoryEntry.encode(results)
		);
		history.put(entry, history.getOrDefault(entry, 0) + 1);
		return results;
	}

	@Override
	public void printTo(PrintStream out) {
		out.println(this);
		int totalCalls = history.values().stream().mapToInt(Integer::intValue).sum();
		history.entrySet().stream()
				.sorted(comparingByValue(reverseOrder()))
				.map(e -> "\t" + (e.getValue() * 100 / totalCalls) + "% (" + e.getValue() + ") " + e.getKey())
				.forEachOrdered(out::println);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " #" + id;
	}

	public Map<HistoryEntry, Integer> history() {
		return Map.copyOf(history);
	}

	private static final class HistoryEntry {
		private final FunctionFactory site;
		private final boolean[] args;
		private final boolean[] results;
		private final int hash;

		private HistoryEntry(FunctionFactory site, boolean[] args, boolean[] results) {
			this.site = site;
			this.args = args;
			this.results = results;
			hash = site.hashCode() ^ Arrays.hashCode(args) ^ Arrays.hashCode(results);
		}

		private static boolean[] encode(Object[] values) {
			switch (values.length) {
				case 0:
					return EMPTY_BOOL_ARRAY;
				case 1:
					return values[0] instanceof Number ? TRUE_BOOL_ARRAY : FALSE_BOOL_ARRAY;
				default:
					boolean[] array = new boolean[values.length];
					for (int i = 0; i < array.length; i++) {
						array[i] = values[i] instanceof Number;
					}
					return array;
			}
		}

		@Override
		public String toString() {
			return site + ": "
					+ IntStream.range(0, args.length)
					.mapToObj(i -> args[i] ? "number" : "object")
					.collect(Collectors.joining(", ", "[", "]"))
					+ " -> "
					+ IntStream.range(0, results.length)
					.mapToObj(i -> results[i] ? "number" : "object")
					.collect(Collectors.joining(", ", "[", "]"));
		}

		@Override
		public int hashCode() {
			return hash;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o.getClass() != HistoryEntry.class) {
				return false;
			}
			var entry = (HistoryEntry) o;
			return entry.hash == hash
					&& Arrays.equals(args, entry.args)
					&& Arrays.equals(results, entry.results)
					&& site.equals(entry.site);
		}
	}
}
