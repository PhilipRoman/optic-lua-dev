package optic.lua

import groovy.transform.CompileStatic

@CompileStatic
class BinaryTreesTest extends GroovyTestCase {
    void testBinaryTrees() {
        def program = new SampleProgram("samples/binary-trees.lua")
        def result = program.run()
        assert result[0].contains("check: 131071")
        assert result[1].contains("check: 1015808")
        assert result[2].contains("check: 1040384")
        assert result[3].contains("check: 1046528")
        assert result[4].contains("check: 1048064")
        assert result[5].contains("check: 1048448")
        assert result[6].contains("check: 1048544")
        assert result[7].contains("check: 65535")
    }
}
