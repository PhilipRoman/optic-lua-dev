# Multiple File Compilation

One or more Lua files can be compiled to a `Bundle`, which contains a 
mapping from each file path to its compiled class. Any file from the
bundle can be invoked by it's path. When a bundled file calls `require`,
the call is forwarded to the current bundle, which looks for a compiled
class with the given key according to the rules of `package.path`.

### Example:
```
files: {
    ./main.lua
    /path/to/lib/library.lua
}

bundle = {
    "main.lua": main_dlua.class
    "library.lua": _spath_sto_slib_slibrary_dlua.class
}

bundle.path = "?.lua;/path/to/lib/?.lua"

bundle.require("main"):
    (search using "?.lua"): compiled version of "main.lua" is executed
    
bundle.require("library"):
    (search using "?.lua"): entry for "library.lua" not found in bundle
    (search using "/path/to/lib/?.lua"): compiled version of "/path/to/lib/library.lua" is executed
```

If the compiler is included in the runtime, it should also be possible to fall back to a
file system lookup like standard Lua does, dynamically compile the file and add it to the
bundle.
