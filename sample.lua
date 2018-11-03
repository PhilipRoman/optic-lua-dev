N = 5
local sum = 0
for i = 1, N do
    for j = 1, N do
        sum = sum + i * j
    end
end
print(sum)

oneTwoThree = function(x, y, z, ...)
    return 1, 2, 3, ...
end

function createTable(...)
    local t = {
        foo = "bar",
        ...
    }
    t[4] = 0
    foo, t.foo = "baz", "baz"
    return t
end

print(oneTwoThree())
do
    local foo = createTable()
    local a, b, c = oneTwoThree()
end