package optic.lua.asm

/**
 * Represents a location which may be assigned a value.
 * Can be either a [name][Name] or
 * a [table field][TableField]
 * represented as two registers: table and key.
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
