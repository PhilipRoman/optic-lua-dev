package optic.lua.asm

import groovy.transform.CompileStatic
import optic.lua.optimization.ProvenType

@CompileStatic
class RegisterFactoryTest extends GroovyTestCase {
    void testCreateVararg() {
        def r = RegisterFactory.createVararg()
        assert r.isVararg()
        assert !r.isUnused()
        assert r != RegisterFactory.createVararg()
    }

    void testCreate() {
        def r = RegisterFactory.create(ProvenType.INTEGER)
        assert !r.isVararg()
        assert !r.isUnused()
        assert r != RegisterFactory.create(ProvenType.INTEGER)
    }

    void testUnused() {
        def r = RegisterFactory.unused()
        assert r.isVararg()
        assert r.isUnused()
    }
}
