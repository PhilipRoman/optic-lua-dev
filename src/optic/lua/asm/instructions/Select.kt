package optic.lua.asm.instructions

import optic.lua.asm.RValue
import optic.lua.asm.Register
import optic.lua.asm.Step
import java.util.function.Consumer

class Select(val out: Register, val varargs: Register, val n: Int) : Step {
    override fun toString(): String {
        return "$out = select($varargs, $n)"
    }

    override fun modified(): Register? = out

    override fun forEachObserved(action: Consumer<RValue>) = action.accept(varargs)
}
