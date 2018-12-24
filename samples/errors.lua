function func(x)
    if x then
        return x
    end
    if x == nil then
        error("message")
    end
end
local foo, bar = pcall(func, nil)
assert(bar == "message")
