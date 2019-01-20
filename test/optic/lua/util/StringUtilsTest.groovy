package optic.lua.util

class StringUtilsTest extends GroovyTestCase {
    void testEscape() {
        assert StringUtils.escape("foo\n") == "foo\\n"
        assert StringUtils.escape("foo\f") == "foo\\f"
        assert StringUtils.escape("foo\b") == "foo\\b"
        assert StringUtils.escape("foo") == "foo"
        assert StringUtils.escape("\r") == "\\r"
        assert StringUtils.escape("") == ""
        assert StringUtils.escape("\t") == "\\t"
        assert StringUtils.escape("\'") == "\\'"
        assert StringUtils.escape("\"") == "\\\""
    }
}
