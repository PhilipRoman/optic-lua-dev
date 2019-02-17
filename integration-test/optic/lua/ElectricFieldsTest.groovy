package optic.lua

import groovy.transform.CompileStatic

@CompileStatic
class ElectricFieldsTest extends GroovyTestCase {
    void testElectricFields() {
        def program = new SampleProgram("samples/electric-fields.lua")
        assertEquals(program.run().first().toDouble(), 0.0011350634653437934d, 1e-10d)
    }
}
