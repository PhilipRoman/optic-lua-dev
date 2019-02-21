package optic.lua.asm

import groovy.transform.CompileStatic

@CompileStatic
class RegisterFactoryTest extends GroovyTestCase {
    void testCreateVararg() {
        def r = RegisterFactory.createVararg()
        assert r.getVararg()
        assert !r.isUnused()
        assert r != RegisterFactory.createVararg()
    }

    void testCreate() {
        def r = RegisterFactory.create()
        assert !r.getVararg()
        assert !r.isUnused()
        assert r != RegisterFactory.create()
    }

    void testUnused() {
        def r = RegisterFactory.unused()
        assert r.getVararg()
        assert r.isUnused()
    }
}
