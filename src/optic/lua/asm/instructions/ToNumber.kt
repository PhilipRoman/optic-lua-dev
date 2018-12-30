package optic.lua.asm.instructions

import optic.lua.asm.Register
import optic.lua.asm.Step
import java.util.function.Consumer

class ToNumber(val source: Register, val target: Register) : Step {
    override fun toString(): String {
        return "$target = number($source)"
    }

    override fun modified(): Register? = target

    override fun forEachObserved(action: Consumer<Register>) = action.accept(source)
}