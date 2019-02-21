package optic.lua.optimization

import groovy.transform.CompileStatic

@CompileStatic
class ProvenTypeTest extends GroovyTestCase {
    void testAnd() {
        assert (ProvenType.INTEGER & ProvenType.NUMBER) == ProvenType.NUMBER
        assert (ProvenType.INTEGER & ProvenType.INTEGER) == ProvenType.INTEGER
        assert (ProvenType.INTEGER & ProvenType.OBJECT) == ProvenType.OBJECT

        assert (ProvenType.NUMBER & ProvenType.NUMBER) == ProvenType.NUMBER
        assert (ProvenType.NUMBER & ProvenType.INTEGER) == ProvenType.NUMBER
        assert (ProvenType.NUMBER & ProvenType.OBJECT) == ProvenType.OBJECT

        assert (ProvenType.OBJECT & ProvenType.NUMBER) == ProvenType.OBJECT
        assert (ProvenType.OBJECT & ProvenType.INTEGER) == ProvenType.OBJECT
        assert (ProvenType.OBJECT & ProvenType.OBJECT) == ProvenType.OBJECT
    }
}
