package optic.lua.runtime

import groovy.transform.CompileStatic

@CompileStatic
class LineNumberTableTest extends GroovyTestCase {
    void testTranslateToLuaLine() {
        def t1 = new LineNumberTable([1, 2, 3, 4, 5] as int[])
        assert t1.translateToLuaLine(1) == 1
        assert t1.translateToLuaLine(3) == 3
        assert t1.translateToLuaLine(5) == 5

        def t2 = new LineNumberTable([5, 6, 7, 8, 9] as int[])
        assert t2.translateToLuaLine(1) == 1
        assert t2.translateToLuaLine(3) == 1
        assert t2.translateToLuaLine(5) == 1
        assert t2.translateToLuaLine(6) == 2
        assert t2.translateToLuaLine(7) == 3
        assert t2.translateToLuaLine(10) == 5
        assert t2.translateToLuaLine(50) == 5
    }
}
