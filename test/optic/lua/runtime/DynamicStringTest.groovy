package optic.lua.runtime

import groovy.transform.CompileStatic

@CompileStatic
class DynamicStringTest extends GroovyTestCase {
    void testValueOf() {
        assert DynamicString.of("abc").value() == "abc"
        assert DynamicString.of("").value() == ""
        assert DynamicString.of("2").value() == "2"
        assert DynamicString.of("↓").value() == "↓"
    }

    void testSubFromTo() {
        assert DynamicString.of("12345").sub(1, 3).value() == "123"
        assert DynamicString.of("12345").sub(4, 4).value() == "4"
        assert DynamicString.of("12345").sub(10, 124).value() == ""
        assert DynamicString.of("12345").sub(1, -1).value() == "12345"
        assert DynamicString.of("12345").sub(1, -3).value() == "123"
        assert DynamicString.of("12345").sub(3, -3).value() == "3"
        assert DynamicString.of("12345").sub(-3, 3).value() == "3"
        assert DynamicString.of("12345").sub(-3, 4).value() == "34"
        assert DynamicString.of("12345").sub(-1, 4).value() == ""
    }

    void testSubFrom() {
        assert DynamicString.of("12345").sub(1).value() == "12345"
        assert DynamicString.of("12345").sub(4).value() == "45"
        assert DynamicString.of("12345").sub(10).value() == ""
        assert DynamicString.of("12345").sub(-3).value() == "345"
        assert DynamicString.of("12345").sub(-20).value() == "12345"
    }
}
