package optic.lua.asm.instructions

import optic.lua.asm.RValue
import optic.lua.asm.Register
import optic.lua.asm.Step

class Assign(public val result: Register, public val value: RValue) : Step {
    override fun modified(): Register? = result
}
