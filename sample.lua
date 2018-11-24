N = 5
local sum = 0
for i = 1, N do
    for j = 1, N do
        sum = sum + i * j
    end
end
print(sum)

oneTwoThree = function(x, y, z, ...)
    if x == 1 and y ~= 1 or x >= 3 then
        local a = {print, foo = print}
        a.foo("Hello!")
    elseif foo then
        print(x)
    else
        print(y)
    end
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

print(createTable(), oneTwoThree())
do
    local foo = createTable()
    local a, b, c = oneTwoThree(), 3
end