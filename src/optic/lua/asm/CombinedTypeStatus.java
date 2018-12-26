package optic.lua.asm;

import optic.lua.optimization.TypeStatus;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Supplier;

public class CombinedTypeStatus {
	private final Collection<@NotNull Supplier<@NotNull TypeStatus>> sources = new ArrayList<>();
	private static final EnumMap<TypeStatus, Supplier<TypeStatus>> supplierCache = new EnumMap<>(Map.of(
			TypeStatus.NONE, () -> TypeStatus.NONE,
			TypeStatus.NUMBER, () -> TypeStatus.NUMBER,
			TypeStatus.OBJECT, () -> TypeStatus.OBJECT
	));

	@SafeVarargs
	public static CombinedTypeStatus of(Supplier<TypeStatus>... sources) {
		var cts = new CombinedTypeStatus();
		cts.sources.addAll(Arrays.asList(sources));
		return cts;
	}

	public TypeStatus get() {
		TypeStatus acc = TypeStatus.NONE;
		for (Supplier<TypeStatus> source : sources) {
			TypeStatus status = source.get();
			acc = acc.and(status);
			// shortcut
			if (acc == TypeStatus.OBJECT) {
				return TypeStatus.OBJECT;
			}
		}
		return acc;
	}

	public void add(@NotNull Supplier<TypeStatus> supplier) {
		sources.add(supplier);
	}

	public void add(TypeStatus value) {
		sources.add(Objects.requireNonNull(supplierCache.get(value)));
	}

	@Override
	public String toString() {
		return get().toString();
	}
}
