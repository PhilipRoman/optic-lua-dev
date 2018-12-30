package optic.lua.asm.instructions

import optic.lua.asm.Register
import optic.lua.asm.Step
import optic.lua.asm.VariableInfo
import java.util.*

class Read(val register: Register, val sourceInfo: VariableInfo) : Step {
    override fun toString(): String {
        val modeName = sourceInfo.mode.toString().toLowerCase();
        return "get-$modeName $register = ${sourceInfo.name}"
    }

    fun getName(): String {
        return sourceInfo.name
    }

    override fun modified(): Register? = register
}
