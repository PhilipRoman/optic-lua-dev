package optic.lua.runtime

import groovy.transform.CompileStatic

@CompileStatic
class StandardLibraryTest extends GroovyTestCase {
    void testToString() {
        assert StandardLibrary.toString("foo").toString() == "foo"
        assert StandardLibrary.toString(null).toString() == "nil"
        assert Double.parseDouble(StandardLibrary.toString(12345.6d)) == 12345.6d
    }

    void testToNumber() {
        assert StandardLibrary.toNumber(0.3) == 0.3
        assert StandardLibrary.toNumber(0) == 0
        assert StandardLibrary.toNumber("99") == 99
        assert StandardLibrary.toNumber(Double.valueOf(2.4d)) == 2.4d
        assert StandardLibrary.toNumber("foo") == null
        assert StandardLibrary.toNumber((Object) null) == null
        assert StandardLibrary.toNumber(Thread.currentThread()) == null
    }

    void testTableConcat() {
        assert StandardLibrary.tableConcat(LuaTable.ofArray(["a", "b", "c"])).toString() == "abc"
        assert StandardLibrary.tableConcat(LuaTable.ofArray(["a", "b", null, "c"])).toString() == "ab"
        assert StandardLibrary.tableConcat(LuaTable.ofArray([null, "a", "b", "c"])).toString().isEmpty()
        assert StandardLibrary.tableConcat(LuaTable.ofArray([])).toString().isEmpty()
        assert StandardLibrary.tableConcat(LuaTable.ofMap(1: "foo", 2: "bar", 5: "baz")).toString() == "foobar"
        assert StandardLibrary.tableConcat(LuaTable.ofMap(2: "foo", 3: "bar")).toString() == ""
        shouldFail {
            StandardLibrary.tableConcat(LuaTable.ofArray([1, 2, 3, true]))
        }
        shouldFail {
            StandardLibrary.tableConcat(LuaTable.ofArray([1, 2, 3, Thread.currentThread()]))
        }
        shouldFail {
            StandardLibrary.tableConcat((LuaTable) null)
        }
    }

    void testPrint() {
        // TODO
    }
}
