package optic.lua.optimization

import groovy.transform.CompileStatic

@CompileStatic
class TypeStatusTest extends GroovyTestCase {
    void testAnd() {
        assert (TypeStatus.UNKNOWN & TypeStatus.UNKNOWN) == TypeStatus.UNKNOWN
        assert (TypeStatus.UNKNOWN & TypeStatus.NUMBER) == TypeStatus.NUMBER
        assert (TypeStatus.UNKNOWN & TypeStatus.OBJECT) == TypeStatus.OBJECT
        assert (TypeStatus.NUMBER & TypeStatus.UNKNOWN) == TypeStatus.NUMBER
        assert (TypeStatus.NUMBER & TypeStatus.NUMBER) == TypeStatus.NUMBER
        assert (TypeStatus.NUMBER & TypeStatus.OBJECT) == TypeStatus.OBJECT
        assert (TypeStatus.OBJECT & TypeStatus.UNKNOWN) == TypeStatus.OBJECT
        assert (TypeStatus.OBJECT & TypeStatus.NUMBER) == TypeStatus.OBJECT
        assert (TypeStatus.OBJECT & TypeStatus.OBJECT) == TypeStatus.OBJECT
    }
}
