local a = 1
a = 2
print(a)

local b = 1
local b = 2
print(b)

--local c
c = 2
print(c)

local d = nil
local function d()
end
print(type(d))

local e = nil
function e() end
print(type(e))
print(_ENV.e)
