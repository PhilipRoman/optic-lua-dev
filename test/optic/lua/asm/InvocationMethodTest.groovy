package optic.lua.asm

import groovy.transform.CompileStatic

import static ExprNode.number
import static optic.lua.asm.ExprList.exprList
import static optic.lua.asm.InvocationMethod.*
import static optic.lua.optimization.StaticType.NUMBER

@CompileStatic
class InvocationMethodTest extends GroovyTestCase {
    void testTypeInfo() {
        assert ADD.typeInfo(number(3.1d), exprList(number(4.2d))) == NUMBER
        assert DIV.typeInfo(number(3.1d), exprList(number(4.2d))) == NUMBER
        assert ADD.typeInfo(number(3.1d), exprList(number(4.2d))) == NUMBER
        assert MUL.typeInfo(number(3.1d), exprList(number(4.2d))) == NUMBER
    }
}
