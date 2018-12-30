package optic.lua.asm.instructions

import optic.lua.asm.Register
import optic.lua.asm.Step
import java.util.function.Consumer

class Operator : Step {
    val a: Register?
    val b: Register
    val target: Register
    val symbol: String

    constructor(a: Register, b: Register, target: Register, symbol: String) {
        this.a = a
        this.b = b
        this.target = target
        this.symbol = symbol
    }

    constructor(b: Register, target: Register, symbol: String) {
        this.a = null
        this.b = b
        this.target = target
        this.symbol = symbol
    }

    override fun toString(): String {
        return "op $target = ${a ?: ""} $symbol $b"
    }

    override fun modified(): Register? = target

    override fun forEachObserved(action: Consumer<Register>) {
        if (a != null) {
            action.accept(a)
        }
        action.accept(b)
    }
}
