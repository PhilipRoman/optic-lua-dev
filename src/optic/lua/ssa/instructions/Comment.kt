package optic.lua.ssa.instructions

import optic.lua.ssa.Step

class Comment(val text: String) : Step {
    override fun toString(): String {
        return "// $text"
    }
}
