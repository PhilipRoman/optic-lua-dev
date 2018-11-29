package optic.lua.asm

/**
 * Represents a location which may be assigned a value.
 * Can be either a variable (represented by a string) or
 * a table assignment in form of `table[key]`
 * represented as two [registers][Register]: table and key.
 */
interface LValue {

    class TableField internal constructor(val table: Register, val key: Register) : LValue {
        override fun toString(): String {
            return table.toString() + "[" + key + "]"
        }
    }

    class Name internal constructor(private val name: String) : LValue {
        override fun toString(): String {
            return name
        }

        fun name(): String {
            return name
        }
    }
}
