package optic.lua.asm;

import java.util.*;

public final class FlatExpr {
	private final List<Step> block;
	private final RValue value;

	public FlatExpr(List<Step> block, RValue value) {
		this.block = List.copyOf(block);
		this.value = value;
	}

	public RValue value() {
		return value;
	}

	public List<Step> block() {
		return block;
	}

	RValue applyTo(List<Step> list) {
		list.addAll(block);
		return value;
	}

	FlatExpr discardRemaining() {
		var r = value.discardRemaining();
		var temp = new ArrayList<Step>(block.size() + r.block.size());
		temp.addAll(block);
		temp.addAll(r.block);
		return new FlatExpr(temp, r.value);
	}
}
