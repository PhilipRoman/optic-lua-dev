package optic.lua

import groovy.transform.CompileStatic

@CompileStatic
class SetEnvTest extends GroovyTestCase {
    void testSetEnv() {
        def program = new SampleProgram("samples/set-env.lua")
        def result = program.run()
        assert result[0..1] == ["value1", "value2"]
    }
}
