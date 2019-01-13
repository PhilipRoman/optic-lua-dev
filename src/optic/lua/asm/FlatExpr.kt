package optic.lua.asm

class FlatExpr(private val block: List<Step>, private val value: Register) {
    fun value(): Register = value
    fun block(): List<Step> = block

    fun applyTo(list: MutableList<Step>): Register {
        list.addAll(block)
        return value
    }

    fun discardRemaining(): FlatExpr {
        if (value.isVararg) {
            val r = value.discardRemaining()
            return FlatExpr(block + r.block, r.value)
        }
        return this
    }
}
