package optic.lua.verify;

import optic.lua.CompilerPlugin;
import optic.lua.asm.*;
import optic.lua.messages.*;

import java.util.*;

/**
 * Verifies that all registers are read only once. This verifier is very fast and can verify
 * the entire program in less than a millisecond.
 */
public class SingleRegisterUseVerifier implements CompilerPlugin {
	private final AsmBlock block;
	private final Context context;

	public SingleRegisterUseVerifier(AsmBlock block, Context context) {
		this.block = block;
		this.context = context;
	}

	@Override
	public boolean concurrent() {
		// even though this plugin can be run concurrently, benchmarks have shown that performance decreases
		// drastically (x10) when running concurrently
		return false;
	}

	@Override
	public AsmBlock apply() throws CompilationFailure {
		if (!context.options().get(StandardFlags.VERIFY)) {
			return block;
		}
		IdentityHashMap<Register, Step> unique = new IdentityHashMap<>(256);
		Map<Register, Step> duplicates = new HashMap<>(0);
		block.forEachRecursive(step -> {
			for (Register r : step.observed()) {
				if (unique.containsKey(r)) {
					duplicates.put(r, step);
				} else {
					unique.put(r, step);
				}
			}
		});
		if (!duplicates.isEmpty()) {
			for (var entry : duplicates.entrySet()) {
				var register = entry.getKey();
				var step = entry.getValue();
				var msg = Message.createError(String.format(
						"Register %s is observed again at [%s], (previously observed at [%s])",
						register, step, unique.get(register)));
				context.reporter().report(msg);
			}
			throw new CompilationFailure();
		}
		return block;
	}

	@Override
	public String toString() {
		return getClass().getName();
	}
}
