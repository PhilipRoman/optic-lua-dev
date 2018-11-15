package optic.lua.asm.instructions

import optic.lua.asm.Step

class Comment(val text: String) : Step {
    override fun toString(): String {
        return "// $text"
    }
}
