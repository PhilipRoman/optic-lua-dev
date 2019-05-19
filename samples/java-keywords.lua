local a = assert
local volatile, class, interface, L_test, assert, _ = 1, 2, 3, 4, 5, 6
a(volatile == 1)
a(class == 2)
a(interface == 3)
a(L_test == 4)
a(assert == 5)
a(_ == 6)