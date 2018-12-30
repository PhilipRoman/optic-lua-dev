package optic.lua.verify;

import optic.lua.CompilerPlugin;
import optic.lua.asm.*;
import optic.lua.messages.*;

import java.util.*;

/**
 * Verifies that all registers are assigned only once. This verifier is very fast and can verify
 * the entire program in less than a millisecond.
 */
public class SingleAssignmentVerifier implements CompilerPlugin {
	private final AsmBlock block;
	private final Context context;

	public SingleAssignmentVerifier(AsmBlock block, Context context) {
		this.block = block;
		this.context = context;
	}

	@Override
	public boolean concurrent() {
		// even though this plugin can be run concurrently, benchmarks have shown that performance decreases
		// drastically (x20) when running concurrently
		return false;
	}

	@Override
	public AsmBlock apply() throws CompilationFailure {
		if(!context.options().contains(Option.VERIFY)) {
			return block;
		}
		var modified = new ArrayList<Register>(256);
		block.forEachRecursive(step -> {
			Register m = step.modified();
			if (m != null) modified.add(m);
		});
		Set<Register> unique = new HashSet<>(modified.size());
		List<Register> duplicates = new ArrayList<>();
		for (Register m : modified) {
			if (m != null && unique.contains(m)) {
				duplicates.add(m);
			} else {
				unique.add(m);
			}
		}
		if (!duplicates.isEmpty()) {
			var msg = Message.createError("Registers " + duplicates + " are assigned more than once!");
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
