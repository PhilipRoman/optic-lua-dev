package optic.lua.ssa.instructions


import optic.lua.ssa.Register
import optic.lua.ssa.Step

class LoadConstant(val target: Register, val constant: Any?) : Step {
    init {
        if(constant != null && constant !is Boolean && constant !is Double && constant !is String) {
            throw IllegalArgumentException("Wrong type for constant: ${constant::class}")
        }
    }

    override fun toString(): String {
        return "constant $target = ${constant?:"nil"}"
    }
}
