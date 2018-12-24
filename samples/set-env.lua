key = "value1"
print(key)
local print = print
_ENV = {
    key = "value2"
}
print(key)
