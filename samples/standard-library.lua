assert(string.sub("abc", 2, 2) == "b")
assert(string.sub("abc", 2, -2) == "b")
assert(string.sub("abc", -2, 2) == "b")
assert(string.sub("abc", 1, 3) == "abc")
assert(string.sub("abc", -3, -1) == "abc")
assert(string.sub("abc", -2, -2) == "b")
assert(string.sub("abc", -1, -3) == "")
assert(string.sub("abc", 3, 1) == "")
assert(string.sub("abc", 1, 10) == "abc")
assert(string.sub("abc", 2, nil) == "bc")

print "1"

assert(string.lower("aBC") == "abc")
assert(string.upper("ABc") == "ABC")

print "2"

local function bytes(str, from, to)
    return table.concat({string.byte(str, from, to)}, " ")
end

assert(string.byte("abc", -1) == string.byte("c"))
assert(bytes("abc") == "97")
assert(bytes("abc", 2) == "98")
assert(bytes("abc", 2, 2) == "98")
assert(bytes("abc", 2, 3) == "98 99")
assert(bytes("abc", -3, -1) == "97 98 99")
assert(bytes("abc", -1, -3) == "")
assert(bytes("abc", 1, 10) == "97 98 99")

print "3"

assert(string.rep("a", 3) == "aaa")
assert(string.rep("a", 2) == "aa")
assert(string.rep("a", 1) == "a")
assert(string.rep("a", 0) == "")
assert(string.rep("a", -1) == "")

print "4"

assert(string.format("%d", 3) == "3")
assert(string.format("(%s)", "foo") == "(foo)")

print "5"

assert(tostring(nil) == "nil")
assert(tostring("foo") == "foo")
assert(tostring(nil) == "nil")
assert(tonumber(tostring(3)) == 3)

print "6"

assert(string.len("abc") == 3)
assert(string.len(1234) == 4)
assert(string.len("") == 0)
