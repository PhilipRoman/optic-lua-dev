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
