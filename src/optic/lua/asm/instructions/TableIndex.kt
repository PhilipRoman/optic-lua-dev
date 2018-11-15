package optic.lua.asm.instructions

import optic.lua.asm.Register
import optic.lua.asm.Step

class TableIndex(val table: Register, val key: Register, val out: Register) : Step {
    override fun toString(): String {
        return "lookup $out = $table[$key]"
    }
}
