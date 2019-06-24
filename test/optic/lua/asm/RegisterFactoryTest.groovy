package optic.lua.asm

import groovy.transform.CompileStatic
import optic.lua.optimization.StaticType

@CompileStatic
class RegisterFactoryTest extends GroovyTestCase {
    void testCreate() {
        def r = Register.ofType(StaticType.INTEGER)
        assert !r.isVararg()
        assert r != Register.ofType(StaticType.INTEGER)
    }
}
