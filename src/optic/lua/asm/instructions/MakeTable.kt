package optic.lua.asm.instructions

import optic.lua.asm.Register
import optic.lua.asm.Step
import java.util.function.Consumer

class MakeTable(val values: Map<Register, Register>, val result: Register) : Step {
    override fun toString(): String {
        return "table $result = $values"
    }

    override fun modified(): Register? = result

    override fun forEachObserved(action: Consumer<Register>) {
        values.forEach { k, v -> action.accept(k); action.accept(v)}
    }
}
