local mode, foo = 1
if mode == 1 then
    foo = "one"
elseif mode == 2 then
    foo = "two"
elseif mode == 3 then
    foo = "four"
else
    foo = "something"
end
print(foo)
