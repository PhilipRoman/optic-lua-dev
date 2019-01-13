local function indexed(func)
    local index = 0
    return function(e)
        index = index + 1
        func(e, index)
    end
end

local function divisible(x, n)
    return x % n == 0
end

local function forEach(tb, f)
    for i = 1, #tb do
        f(tb[i])
    end
end

local tb, result = {}, {}
local mapper = function(x)
    return x * x
end
for i = 1, 1000000 do
    tb[i] = math.sqrt(i)
end
local pattern = 0
forEach(tb, indexed(function(e, i)
    if divisible(i, 31) then
        pattern = pattern | i
        mapper = function(x)
            return x / 2
        end
    elseif divisible(i, 17) then
        pattern = pattern ~ i
        mapper = function(x)
            return math.sqrt(x) * 2
        end
    elseif divisible(i, 53) then
        pattern = pattern & i
        mapper = function(x)
            return 1 .. x
        end
    else
        pattern = (pattern << (i % 3)) & (i >> (pattern % 5))
    end
    result[i] = mapper(e)
end))
print(pattern)
local sum = 0
forEach(result, indexed(function(e, i)
    sum = sum + (e / math.sqrt(i + e)) ^ 2
end))
print(sum)
