package optic.lua.asm.instructions

import optic.lua.asm.Register
import optic.lua.asm.Step
import java.util.*

class TableRead(val table: Register, val key: Register, val out: Register) : Step {
    override fun toString(): String {
        return "get $out = $table[$key]"
    }

    override fun modified(): Register? = out
}
