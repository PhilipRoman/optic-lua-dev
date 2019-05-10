local number = 0 -- type = int
local dynamic = (function(x)
    return x
end)(1) -- type = object

for i = 1, 5 do
    print(number + 1) -- only first iteration may use primitive addition
    number = number + dynamic
end