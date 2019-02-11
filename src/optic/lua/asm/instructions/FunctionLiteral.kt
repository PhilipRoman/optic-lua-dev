package optic.lua.asm.instructions

import optic.lua.asm.AsmBlock
import optic.lua.asm.ParameterList
import optic.lua.asm.Register
import optic.lua.asm.Step

@Deprecated("Use RValue.FunctionLiteral in future")
class FunctionLiteral(val body: AsmBlock, val assignTo: Register, val params: ParameterList) : Step {
    override fun toString(): String {
        return "function $assignTo = function($params)"
    }

    override fun children(): List<Step> = body.steps()

    override fun modified(): Register? = assignTo
}
