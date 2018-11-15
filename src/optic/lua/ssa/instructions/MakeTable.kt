package optic.lua.ssa.instructions

import optic.lua.ssa.Register
import optic.lua.ssa.Step

class MakeTable(val values: Map<Register, Register>, val result: Register) : Step {
    override fun toString(): String {
        return "table $result = $values"
    }
}
