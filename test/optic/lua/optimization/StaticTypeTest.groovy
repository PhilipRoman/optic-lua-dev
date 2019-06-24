package optic.lua.optimization

import groovy.transform.CompileStatic

import static optic.lua.optimization.StaticType.*

@CompileStatic
class StaticTypeTest extends GroovyTestCase {
    void testAnd() {
        assert (INTEGER & NUMBER) == NUMBER
        assert (INTEGER & INTEGER) == INTEGER
        assert (INTEGER & OBJECT) == OBJECT

        assert (NUMBER & NUMBER) == NUMBER
        assert (NUMBER & INTEGER) == NUMBER
        assert (NUMBER & OBJECT) == OBJECT

        assert (OBJECT & NUMBER) == OBJECT
        assert (OBJECT & INTEGER) == OBJECT
        assert (OBJECT & OBJECT) == OBJECT

        for (def type in [BOOLEAN, FUNCTION, TABLE, STRING]) {
            assert (INTEGER & type) == OBJECT
            assert (NUMBER & type) == OBJECT
            assert (OBJECT & type) == OBJECT
            assert (type & type) == type
        }
    }
}
