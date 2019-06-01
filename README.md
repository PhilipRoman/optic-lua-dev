[![Codacy Badge](https://api.codacy.com/project/badge/Grade/42ba3cf88ee148caab9261dea45a572a)](https://www.codacy.com?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=PhilipRoman/optic-lua-dev&amp;utm_campaign=Badge_Grade)
# Optic-Lua

Extensible, Ahead-of-Time Lua to JVM Compiler.

## Installation

Clone or download the files from this repository, then build a standalone jar:
```bash
./gradlew shadowJar
```

Launch the compiler to see available options:
```bash
java -jar build/libs/optic-lua.jar --help
```
If you've compiled some Lua code to a .class file, you can run it by adding the compiler jar to the classpath:
```bash
java -cp .:build/libs/optic-lua.jar my_compiled_class
```

## Features
*   Lua 5.3 support
*   Ahead-of-Time compilation from Lua to runnable .class files
*   Compilation of Lua code to Java source code
*   Interactive shell
*   Analyze runtime statistics, such as common parameter types
Note that the compiler does not yet fully implement all of Lua and its standard library. You can track the progress here: [Lua 5.3 implementation](https://github.com/PhilipRoman/optic-lua-dev/projects/1).

## Planned features
*   Zero-overhead FFI to Java code
*   Ability to mix Lua and Java code in the same file
*   Embedding API
