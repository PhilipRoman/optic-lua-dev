package optic.lua.asm

class FlatExpr(private val block: List<Step>, private val value: RValue) {
    fun value(): RValue = value
    fun block(): List<Step> = block

    fun applyTo(list: MutableList<Step>): RValue {
        list.addAll(block)
        return value
    }

    fun discardRemaining(): FlatExpr {
            val r = value.discardRemaining()
            return FlatExpr(block + r.block, r.value)
    }
}
