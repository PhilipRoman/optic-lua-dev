package optic.lua.asm.instructions

import optic.lua.asm.Register
import optic.lua.asm.Step
import java.util.*

class MakeTable(val values: Map<Register, Register>, val result: Register) : Step {
    override fun toString(): String {
        return "table $result = $values"
    }

    override fun modified(): Register? = result
}
