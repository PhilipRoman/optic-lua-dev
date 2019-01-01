local a = {}
a.b = {}
a.b.c = {}
a.b.c.d = print
a.b.c.foo = "Foo!"
print(a.b.c.foo)
a.b.c.d("Hello!")
