package optic.lua.ssa.instructions

import optic.lua.ssa.Register
import optic.lua.ssa.Step

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
}
