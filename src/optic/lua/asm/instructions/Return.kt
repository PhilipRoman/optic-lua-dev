package optic.lua.asm.instructions

import optic.lua.asm.Register
import optic.lua.asm.Step
import java.util.function.Consumer

class Return(val registers: List<Register>) : Step {
    override fun toString(): String {
        return "return $registers"
    }

    override fun forEachObserved(action: Consumer<Register>) = registers.forEach(action)
}
