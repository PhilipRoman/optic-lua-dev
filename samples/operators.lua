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
