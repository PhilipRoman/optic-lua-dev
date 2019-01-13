package optic.lua

import groovy.transform.CompileStatic

@CompileStatic
class IfElseChainTest extends GroovyTestCase {
    void testIfElseChain() {
        def program = new SampleProgram("samples/if-else-chain.lua")
        def result = program.run()
        assert result[0] == "more than six"
    }
}
