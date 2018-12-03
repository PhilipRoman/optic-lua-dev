package optic.lua.asm

import groovy.transform.CompileStatic

@CompileStatic
class ParameterListTest extends GroovyTestCase {
    void testParse() {
        // TODO
    }

    void testHasVarargs() {
        assert ParameterList.of(["..."]).hasVarargs()
        assert ParameterList.of(["foo", "..."]).hasVarargs()
        assert ParameterList.of(["foo", "bar", "..."]).hasVarargs()
        assert !ParameterList.of([]).hasVarargs()
        assert !ParameterList.of(["foo"]).hasVarargs()
        assert !ParameterList.of(["foo", "bar"]).hasVarargs()
    }

    void testIndexOf() {
        assert OptionalInt.of(1) == ParameterList.of(["foo", "bar"]).indexOf("bar")
        assert OptionalInt.of(0) == ParameterList.of(["foo", "bar"]).indexOf("foo")
        assert OptionalInt.empty() == ParameterList.of([]).indexOf("foo")
        assert OptionalInt.empty() == ParameterList.of(["bar"]).indexOf("foo")
    }

    void testList() {
        assert ["foo", "bar", "baz"] == ParameterList.of(["foo", "bar", "baz"]).list()
        assert [] == ParameterList.of([]).list()
    }
}
