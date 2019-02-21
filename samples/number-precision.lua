local n = 1
for _ = 1, 1000 do
    n = n / 2.000001
end
for _ = 1, 1000 do
    n = n * 2.000001
end
print(n)

for _ = 1, 10000 do
    n = n / 2.000001
end
for _ = 1, 10000 do
    n = n * 2.000001
end
print(n)

n = 0
local X = 1000000
for _ = 1, X do
    n = n + 1 / X
end
print(n)

print((2^53 - 0.501) == 2^53)
print((2^53 - 0.5) == 2^53)
print((2^53 + 1) == 2^53)
print((2^53 + 1.1) == 2^53)
