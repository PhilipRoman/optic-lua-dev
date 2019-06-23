package optic.lua.asm

import groovy.transform.CompileStatic
import optic.lua.optimization.ProvenType

@CompileStatic
class RegisterFactoryTest extends GroovyTestCase {
    void testCreate() {
        def r = Register.ofType(ProvenType.INTEGER)
        assert !r.isVararg()
        assert r != Register.ofType(ProvenType.INTEGER)
    }
}
