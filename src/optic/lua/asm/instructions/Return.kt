package optic.lua.asm.instructions

import optic.lua.asm.RValue
import optic.lua.asm.Step
import java.util.function.Consumer

class Return(val registers: List<RValue>) : Step {
    override fun toString(): String {
        return "return $registers"
    }

    override fun forEachObserved(action: Consumer<RValue>) = registers.forEach(action)
}
