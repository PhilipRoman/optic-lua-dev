package optic.lua.optimization

import groovy.transform.CompileStatic

@CompileStatic
class ProvenTypeTest extends GroovyTestCase {
    void testAnd() {
        assert (ProvenType.UNKNOWN & ProvenType.UNKNOWN) == ProvenType.UNKNOWN
        assert (ProvenType.UNKNOWN & ProvenType.NUMBER) == ProvenType.NUMBER
        assert (ProvenType.UNKNOWN & ProvenType.OBJECT) == ProvenType.OBJECT
        assert (ProvenType.NUMBER & ProvenType.UNKNOWN) == ProvenType.NUMBER
        assert (ProvenType.NUMBER & ProvenType.NUMBER) == ProvenType.NUMBER
        assert (ProvenType.NUMBER & ProvenType.OBJECT) == ProvenType.OBJECT
        assert (ProvenType.OBJECT & ProvenType.UNKNOWN) == ProvenType.OBJECT
        assert (ProvenType.OBJECT & ProvenType.NUMBER) == ProvenType.OBJECT
        assert (ProvenType.OBJECT & ProvenType.OBJECT) == ProvenType.OBJECT
    }
}
