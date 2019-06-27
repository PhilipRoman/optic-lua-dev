local number = 0 -- type = int
local dynamic = (function(x)
    return x
end)(1) -- type = object

for i = 1, 5 do
    print(number + 1) -- only first iteration may use primitive addition
    number = number + dynamic -- number should become 'object'
end

local root = 1
if true then
    local branch_1 = root + 3
    local branch_2 = root + 2.5
    root = root + 2.5
    assert(branch_1 - 0.5 == branch_2)
end
root = 1
for i = 1, 5 do
    local branch_1 = root + 3
    local branch_2 = root + 2.5
    root = root + 2.5
    assert(branch_1 - 0.5 == branch_2)
end
