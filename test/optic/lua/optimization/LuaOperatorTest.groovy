package optic.lua.optimization


import static optic.lua.optimization.ProvenType.*

class LuaOperatorTest extends GroovyTestCase {
    void testArity() {
        assert LuaOperator.ADD.arity() == 2
        assert LuaOperator.SUB.arity() == 2
        assert LuaOperator.UNM.arity() == 1
        assert LuaOperator.LEN.arity() == 1
        assert LuaOperator.EQ.arity() == 2
        assert LuaOperator.BNOT.arity() == 1
        for (def op : LuaOperator.values()) {
            assert [1, 2].contains(op.arity())
        }
    }

    void testResultType() {
        assert LuaOperator.ADD.resultType(NUMBER, INTEGER) == NUMBER
        assert LuaOperator.ADD.resultType(INTEGER, INTEGER) == INTEGER
        assert LuaOperator.ADD.resultType(INTEGER, OBJECT) == OBJECT
        assert LuaOperator.ADD.resultType(OBJECT, OBJECT) == OBJECT

        assert LuaOperator.MUL.resultType(OBJECT, OBJECT) == OBJECT
        assert LuaOperator.MUL.resultType(INTEGER, OBJECT) == OBJECT
        assert LuaOperator.MUL.resultType(NUMBER, OBJECT) == OBJECT
        assert LuaOperator.MUL.resultType(INTEGER, INTEGER) == INTEGER
        assert LuaOperator.MUL.resultType(NUMBER, INTEGER) == NUMBER
        assert LuaOperator.MUL.resultType(INTEGER, NUMBER) == NUMBER
        assert LuaOperator.MUL.resultType(NUMBER, NUMBER) == NUMBER

        assert LuaOperator.DIV.resultType(NUMBER, NUMBER) == NUMBER
        assert LuaOperator.DIV.resultType(INTEGER, INTEGER) == NUMBER
    }
}
