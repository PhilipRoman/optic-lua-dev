package optic.lua.io

import groovy.transform.CompileStatic

import java.nio.file.Paths

@CompileStatic
class FileExtensionMapperTest extends GroovyTestCase {
    void testCreateValid() {
        FileExtensionMapper.create(".foo", ".bar")
        FileExtensionMapper.create(".lua", ".java")
    }

    void testCreateInvalid() {
        shouldFail {
            FileExtensionMapper.create("foo", "bar")
        }
        shouldFail {
            FileExtensionMapper.create("", "foo")
        }
        shouldFail {
            FileExtensionMapper.create("...", "foo")
        }
        shouldFail {
            FileExtensionMapper.create(".foo", null)
        }
    }

    void testMap() {
        def mapper = FileExtensionMapper.create(".lua", ".java")
        assert Paths.get("/foo/bar.java") == mapper.map(Paths.get("/foo/bar.lua"))
    }
}
