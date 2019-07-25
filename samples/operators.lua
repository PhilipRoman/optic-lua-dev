local a = assert

a(1 + 1 == 2)
a(1 * 1 == 1)
a(6 * 7 == 42)
a(1 - 1 == 0)
a(1 / 1 == 1)
a(1 / 2 == 0.5)
a(5 ^ 3 == 125)
a(5 ^ 0 == 1)
a(5 ^ 1 == 5)
a(5 ^ (-1) == 0.2)
a(5 ^ (-2) == 0.04)
a(2 ^ 2 == 4)
a(2000 ^ 2 == 4000000)
a(2000000 ^ 2 == 4000000000000)
a(2000000000 ^ 2 == 4000000000000000000)
a(1 | 1 == 1)
a(1 & 1 == 1)
a(1 ~ 1 == 0)
a(1 << 1 == 2)
a(1 << 5 == 32)
a(1 >> 1 == 0)
a(1 >> 7 == 0)
a(~1 == -2)
a(~0 == -1)
a(1 > 0.99)
a(1 < 1.01)
a(1 >= 0.99)
a(1 <= 1.01)
a(1 >= 1)
a(1 <= 1)
a(1 == 1)
a(#{} == 0)
a(#{ 1, 2, 3 } == 3)
local t = {}
t[1] = 5
t[3] = 6
a(#t == 1)
local t = {}
for i = 1, 1000 do
    t[i] = math.sqrt(i)
end
a(#t == 1000, #t)

for i = 1, 100 do
    for j = 1, 100 do
        local diff = i ^ (-j) - 1 / (i ^ j)
        a(diff < 0.00000000001, diff)
    end
end

a((5 ^ 29 % 5) == 3)

local pattern = 0

for i = 1, 10 do
    local x = ((pattern) << (i % 3))
    local y = (i >> (5 ^ pattern % 5))
    pattern = x | y
end
assert(pattern == 470, pattern)

pattern = 0
for i = 1, 1000000 do
    if i % 31 == 0 then
        pattern = pattern | i
    elseif i % 17 == 0 then
        pattern = pattern ~ i
    elseif i % 53 == 0 then
        pattern = pattern & i
    else
        pattern = (pattern << (i % 3)) & (i >> (pattern % 5))
    end
end
assert(pattern == 163840, pattern)

local function divisible(x, n)
    return x % n == 0
end

for i = 1, 1000000 do
    assert(divisible(i, 17) == (i % 17 == 0), i)
end

assert((1 or 2) == 1)
assert((1 and 2) == 2)
assert((nil and nil) == nil)
assert((nil and false) == nil)
assert((nil or nil) == nil)
assert((nil or false) == false)
assert(("string" or 1) == "string")
assert((print and true) == true)
assert((true and print) == print)
assert((print or true) == print)
assert((true or print) == true)
for _, v in ipairs { 1, 2, 3, "a", "b", "c", print, 1.2, true, {} } do
    assert(not v == false, v)
    assert((v and 34) == 34, v)
    assert((v or 34) == v, v)
    assert((v and not v) == false, v)
    assert((v and 56 or 65) == 56, v)
end
assert(not nil == true)
assert(not false == true)
-- test lazy evaluation
local function t()
    return true
end
local function g()
    error "Evaluated!"
end
assert((t() or g()) == true)
local function f()
    return false
end
assert((f() and g()) == false)

-- regression test: "Function r-values must only be evaluated once in multi-variable assignment"
local hits = 0
local function f()
    hits = hits + 1
    return 1, 2, 3
end
local a, b, c, d, e = 0, f()
assert(a == 0)
assert(b == 1)
assert(c == 2)
assert(d == 3)
assert(e == nil)
assert(hits == 1)
