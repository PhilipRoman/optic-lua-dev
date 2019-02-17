package optic.lua.asm.instructions

import optic.lua.asm.RValue
import optic.lua.asm.Step
import java.util.function.Consumer

class Return(val values: List<RValue>) : Step {
    override fun toString(): String {
        return "return $values"
    }

    override fun forEachObserved(action: Consumer<RValue>) = values.forEach(action)
}
