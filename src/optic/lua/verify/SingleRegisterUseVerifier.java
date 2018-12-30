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
		if(!context.options().contains(Option.VERIFY)) {
			return block;
		}
		Set<Register> unique = new HashSet<>(256);
		List<Register> duplicates = new ArrayList<>();
		block.forEachRecursive(step -> {
			for (Register r : step.observed()) {
				if (unique.contains(r)) {
					duplicates.add(r);
				} else {
					unique.add(r);
				}
			}
		});
		if (!duplicates.isEmpty()) {
			var msg = Message.createError("Registers " + duplicates + " are observed more than once!");
			context.reporter().report(msg);
			throw new CompilationFailure();
		}
		return block;
	}

	@Override
	public String toString() {
		return getClass().getName();
	}
}
