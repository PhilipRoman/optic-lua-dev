package optic.lua.asm.instructions


import optic.lua.asm.Register
import optic.lua.asm.Step
import java.util.*

class LoadConstant(val target: Register, val constant: Any?) : Step {
    init {
        if(constant != null && constant !is Boolean && constant !is Double && constant !is String) {
            throw IllegalArgumentException("Wrong type for constant: ${constant::class}")
        }
    }

    override fun toString(): String {
        return "constant $target = ${constant?:"nil"}"
    }

    override fun modified(): Register? = target
}
