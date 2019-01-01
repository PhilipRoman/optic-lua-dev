local mode, foo = 7
if mode == 1 then
    foo = "one"
elseif mode == 2 then
    foo = "two"
elseif mode > (3 * 2) then
    foo = "more than six"
else
    foo = "something"
end
print(foo)
