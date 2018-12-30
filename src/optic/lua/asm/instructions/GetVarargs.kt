package optic.lua.asm.instructions

import optic.lua.asm.Register
import optic.lua.asm.Step

class GetVarargs(val to: Register) : Step {
    override fun toString(): String {
        return "varargs $to = ..."
    }

    override fun modified(): Register? = to
}
