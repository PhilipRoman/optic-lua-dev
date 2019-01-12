key = "value1"
print(key)

local f = function()
    local _ENV = 3;
    local g = function()
        _ENV = {}
    end
    g()
end
f()

local print = print
_ENV = {
    key = "value2"
}
print(key)
