local a = {}
a.b = {}
a.b.c = {}
a.b.c.d = print
a.b.c.foo = "Foo!"
print(a.b.c.foo)
a.b.c.d("Hello!")

a = {
	b = function()
		return {
			c = 5
		}
	end
}

assert(type(a) == "table")
assert(type(a.b) == "function")
assert(type(a.b()) == "table")
assert(a.b().c == 5)

obj = {
	name = "Joe";
	method = function(self)
		return "name is " .. self.name
	end
}
assert(obj:method() == "name is Joe")
assert(obj.method {name = "Tom"} == "name is Tom")

obj.method = function(self)
	return self
end

assert(obj:method():method():method() == obj)
assert(obj:method():method().method == obj.method)
