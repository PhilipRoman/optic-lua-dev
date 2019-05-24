package optic.lua.io

import groovy.transform.CompileStatic

@CompileStatic
class JavaToBytecodeCompilerTest extends GroovyTestCase {
    void testCompile() {
        def bytecode = new JavaToBytecodeCompiler().compile("""
public class Foo {
    public void method() {
        System.out.println("Hello!");
    }
}""")
        assert bytecode.length > 100
        assert bytecode[0..3] == [(byte) 0xCA, (byte) 0xFE, (byte) 0xBA, (byte) 0xBE]


        def otherBytecode = new JavaToBytecodeCompiler().compile("""
public class Foo {
}""")
        assert otherBytecode[0..3] == [(byte) 0xCA, (byte) 0xFE, (byte) 0xBA, (byte) 0xBE]
        assert otherBytecode.length < bytecode.length


        shouldFail {
            new JavaToBytecodeCompiler().compile("some garbage here")
        }
    }
}
