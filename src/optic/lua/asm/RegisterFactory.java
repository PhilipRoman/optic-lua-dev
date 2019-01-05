package optic.lua.asm;

import optic.lua.optimization.ProvenType;
import optic.lua.util.Numbers;
import org.jetbrains.annotations.*;

import java.util.ArrayList;

public class RegisterFactory {
	private static final Register UNUSED_REGISTER = new Register("_", true);

	@NotNull
	@Contract(" -> new")
	static Register createVararg() {
		return new Register(true);
	}

	@NotNull
	@Contract(" -> new")
	public static Register create() {
		return new Register(false);
	}

	@NotNull
	public static Register unused() {
		return UNUSED_REGISTER;
	}

	public static FlatExpr constant(String string) {
		var r = create();
		var steps = new ArrayList<Step>(1);
		steps.add(StepFactory.constString(r, string));
		r.updateStatus(ProvenType.OBJECT);
		return new FlatExpr(steps, r);
	}

	public static FlatExpr constant(double number) {
		var r = create();
		var steps = new ArrayList<Step>(1);
		steps.add(StepFactory.constNumber(r, number));
		r.updateStatus(Numbers.isInt(number) ? ProvenType.INTEGER : ProvenType.NUMBER);
		return new FlatExpr(steps, r);
	}

	public static FlatExpr constant(boolean bool) {
		var r = create();
		var steps = new ArrayList<Step>(1);
		steps.add(StepFactory.constBool(r, bool));
		r.updateStatus(ProvenType.OBJECT);
		return new FlatExpr(steps, r);
	}

	public static FlatExpr nil() {
		var r = create();
		var steps = new ArrayList<Step>(1);
		steps.add(StepFactory.constNil(r));
		r.updateStatus(ProvenType.OBJECT);
		return new FlatExpr(steps, r);
	}
}
