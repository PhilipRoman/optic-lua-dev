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
    return {...}
end

print(oneTwoThree())
do
    local foo = createTable()
    local a, b, c = oneTwoThree()
end