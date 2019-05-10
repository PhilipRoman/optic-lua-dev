package optic.lua.runtime

import groovy.transform.CompileStatic

@CompileStatic
class LuaTableTest extends GroovyTestCase {
    void testObject() {
        LuaTable table = LuaTable.ofMap(["key1": "value1",
                                         "key2": "value2",
                                         "key3": "value3"])
        assert table.get("key1") == "value1"
        assert table.get("key2") == "value2"
        assert table.get("key3") == "value3"
        assert table.get("key4") == null
    }

    void testMixed() {
        LuaTable table = LuaTable.ofMap(["key1": "value1",
                                         "key2": "value2",
                                         "key3": "value3",
                                         1     : "i1",
                                         2     : "i2",
                                         3     : "i3"])
        assert table.get("key1") == "value1"
        assert table.get("key2") == "value2"
        assert table.get("key3") == "value3"
        assert table.get("key4") == null
        assert table.length() == 3
        assert table.get(1) == "i1"
        assert table.get(2) == "i2"
        assert table.get(3) == "i3"
        assert table.get(4) == null
        assert table.get(0) == null
        assert table.get(-1) == null
    }

    void testBigArray() {
        LuaTable table = LuaTable.ofArray([])
        for (int i = -50; i < 50; i++) {
            table.set(i, String.valueOf(i))
        }
        assert table.get(0) == "0"
        assert table.get(-50) == "-50"
        assert table.get(-50) == "-50"
        assert table.get(50) == null
    }

    void testLength() {
        LuaTable table = LuaTable.ofArray([])
        for (int i = -50; i < 50; i++) {
            table.set(i, String.valueOf(i))
        }
        assert table.length() == 49
        table.set(11, null)
        assert table.length() == 10
        table.set(11, "11")
        assert table.length() == 49
        table.set(51, "51")
        assert table.length() == 49
        table.set(50, "50")
        assert table.length() == 51
        for (int i = -50; i <= 51; i++) {
            table.set(i, null)
        }
        assert table.length() == 0
    }

    void testNullValues() {
        def table = LuaTable.ofMap(1: null, 2: "foo", 3: "bar")
        assert table.get(1) == null
        assert table.get(2) == "foo"
        assert table.get(3) == "bar"
        assert table.get(4) == null
        assert table.get(0) == null
    }
}
