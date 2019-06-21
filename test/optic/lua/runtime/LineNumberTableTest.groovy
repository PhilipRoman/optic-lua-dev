package optic.lua.runtime

import groovy.transform.CompileStatic

@CompileStatic
class LineNumberTableTest extends GroovyTestCase {
    void testTranslateToLuaLine() {
        def t1 = new LineNumberTable([2, 1, 4, 2, 6, 3] as int[])
        assert t1.translateToLuaLine(1) == -1
        assert t1.translateToLuaLine(2) == 1
        assert t1.translateToLuaLine(5) == 2
        assert t1.translateToLuaLine(6) == 3

        def t2 = new LineNumberTable([20, 10, 40, 20, 60, 30] as int[])
        assert t2.translateToLuaLine(1) == -1
        assert t2.translateToLuaLine(19) == -1
        assert t2.translateToLuaLine(20) == 10
        assert t2.translateToLuaLine(21) == 10
        assert t2.translateToLuaLine(39) == 10
        assert t2.translateToLuaLine(40) == 20
        assert t2.translateToLuaLine(59) == 20
        assert t2.translateToLuaLine(60) == 30
    }
}
