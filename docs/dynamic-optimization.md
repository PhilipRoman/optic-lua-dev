# Dynamic Optimization

## Function Specialization

Each function maintains a construction site identifier. We can assume that two functions
created from the same construction site will usually have equal argument types.

Every time a function is invoked, the invocation is recorded in the history of the
creation site. After a certain number of invocations, we can pick the most common
argument types and compile a special function for the given types. Then, every function
produced from that construction site will have an alternative implementation added to it.

When the function is invoked, it will quickly check if the argument types match the
parameter types of the alternative function, and depending on the result, invoke either
the optimized alternative or the old fallback function body.

## Table Specialization

The same principles can be applied to tables. Each table construction site maintains a
record of common keys in the table. Once the creation site reaches a certain number of
invocations, we can create an optimized table implementation which stores the commonly
accessed fields in class members and also includes a fallback hash map for all other
entries.
