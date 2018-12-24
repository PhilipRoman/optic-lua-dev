local time = os.time
local start = time()
local N = 10000
local sum = 0
for i = 1, N do
    for j = 1, N do
        sum = sum + i * j
    end
end
print(sum)
print("TIME: " .. (time() - start))
