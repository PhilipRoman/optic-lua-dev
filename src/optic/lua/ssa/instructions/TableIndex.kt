package optic.lua.ssa.instructions

import optic.lua.ssa.Register
import optic.lua.ssa.Step

class TableIndex(val table: Register, val key: Register, val out: Register) : Step {
    override fun toString(): String {
        return "lookup $out = $table[$key]"
    }
}
