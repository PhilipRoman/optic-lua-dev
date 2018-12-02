package optic.lua.files


import java.nio.file.Paths

class FileExtensionMapperTest extends GroovyTestCase {
    void testCreateValid() {
        FileExtensionMapper.create(".foo", ".bar")
        FileExtensionMapper.create(".lua", ".java")
    }

//    @Test(expected = IllegalArgumentException)
    void testCreateInvalid() {
        try {
            FileExtensionMapper.create("foo", "bar")
        } catch(IllegalArgumentException ignored) {
            return
        }
        fail()
    }

    void testMap() {
        def mapper = FileExtensionMapper.create(".lua", ".java")
        assertEquals(Paths.get("/foo/bar.java"), mapper.map(Paths.get("/foo/bar.lua")))
    }
}
