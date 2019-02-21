function func(x)
    if x then
        return x
    end
    if x == nil then
        error("message")
    end
end
local _, msg = pcall(func, nil)
print(msg)
