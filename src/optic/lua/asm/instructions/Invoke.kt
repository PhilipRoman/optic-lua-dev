package optic.lua.asm.instructions

import optic.lua.asm.RValue
import optic.lua.asm.Register
import optic.lua.asm.Step

class Invoke(public val result: Register, public val value: RValue, public val method: InvocationMethod, public val arguments: List<RValue>) : Step
