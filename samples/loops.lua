local i, sum = 0, 0
while i < 5 do
    sum = sum + i
    i = i + 1
end
print(sum == 1 + 2 + 3 + 4)
print(i == 5)

i, sum = 0, 0
while i < 5 do
    do
        sum = sum + i
        i = i + 1
    end
end
print(sum == 1 + 2 + 3 + 4)
print(i == 5)

i = 0
repeat
    i = i + 1
until i == 5
print(i == 5)

sum = 0
for _i = 0, 10, 2 do
    sum = sum + _i
end
print(sum == 2 + 4 + 6 + 8 + 10)

-- test that each loop parameter is only evaluated once
local num1, num2, num3 = 0, 0, 0
local function one()
    num1 = num1 + 1
    return 1
end
local function five()
    num2 = num2 + 1
    return 5
end
local function two()
    num3 = num3 + 1
    return 2
end
sum = 0
for _i = one(), five(), two() do
    sum = sum + _i
end
print(sum == 1 + 3 + 5)
print(num1)
print(num2)
print(num3)

-- test for loops with floating-point values
sum = 0
for _i = 0.2, 0.79, 0.3 do
    sum = sum + _i
end
assert(sum == 0.2 + 0.5)

sum = 0
for _i = 1, 3, 0.5 do
    sum = sum + _i
end
assert(sum == 1 + 1.5 + 2 + 2.5 + 3)

sum = 0
for _i = 1, -1.1, -0.5 do
    sum = sum + _i
end
assert(sum == 0)

local keys = {}
for k, v in pairs {a = 1, b = 2, c = 3} do
    keys[#keys + 1] = k
end
assert(#keys == 3, #keys)

sum = 0
for k, v in pairs { 1, 2, 3, 4, 5 } do
    sum = sum + v
    assert(k == v)
end
assert(sum == 15)
