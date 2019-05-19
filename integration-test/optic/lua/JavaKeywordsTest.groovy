package optic.lua

import groovy.transform.CompileStatic

@CompileStatic
class JavaKeywordsTest extends GroovyTestCase {
    void testLoops() {
        def program = new SampleProgram("samples/java-keywords.lua")
        def result = program.run()
        assert result == []
    }
}
