package optic.lua.asm.instructions

import optic.lua.asm.LValue
import optic.lua.asm.Register
import optic.lua.asm.Step

class TableWrite constructor(val field: LValue.TableField, val value: Register) : Step {
    override fun toString(): String {
        return "set $field = $value"
    }
}