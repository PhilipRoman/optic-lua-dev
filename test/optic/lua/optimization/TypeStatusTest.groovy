package optic.lua.optimization

import groovy.transform.CompileStatic

@CompileStatic
class TypeStatusTest extends GroovyTestCase {
    void testAnd() {
        assert (TypeStatus.NONE & TypeStatus.NONE) == TypeStatus.NONE
        assert (TypeStatus.NONE & TypeStatus.NUMBER) == TypeStatus.NUMBER
        assert (TypeStatus.NONE & TypeStatus.OBJECT) == TypeStatus.OBJECT
        assert (TypeStatus.NUMBER & TypeStatus.NONE) == TypeStatus.NUMBER
        assert (TypeStatus.NUMBER & TypeStatus.NUMBER) == TypeStatus.NUMBER
        assert (TypeStatus.NUMBER & TypeStatus.OBJECT) == TypeStatus.OBJECT
        assert (TypeStatus.OBJECT & TypeStatus.NONE) == TypeStatus.OBJECT
        assert (TypeStatus.OBJECT & TypeStatus.NUMBER) == TypeStatus.OBJECT
        assert (TypeStatus.OBJECT & TypeStatus.OBJECT) == TypeStatus.OBJECT
    }
}
