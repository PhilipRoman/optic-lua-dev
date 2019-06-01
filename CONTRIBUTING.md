# Contributing

Fork the project and create a new branch. The name of the branch should be descriptive but short. Some good examples: `line_numbers`, `codegen_refactor`, `parser_fix`, `version_bump`.
Note that the project is under active development and major refactoring is common. If you're going to work on your fork, create an issue first so I don't ruin your work by rewriting Git history.
## Code style & formatting

Generally you shouldn't worry about style or formatting. Since I often develop in the terminal, every so often I have to format the whole repository from my IDE either way.

Some useful guidelines are listed below:
*   Use tabs.
*   Every variable/field/parameter is considered non-null by default (don't do null checks). All nullable values should be documented and/or marked using @Nullable.
*   Every class should be final by default. If a class is non-final, it's assumed to be designed for inheritance.
*   Don't write documentation without a reason.

## Build process

The project uses Gradle build system to produce outputs. Run `gradle test` to run unit and integration tests and `gradle shadowJar` to produce a standalone jar.

The recommended way to run the project is to execute the standalone jar: `java -jar build/libs/optic-lua.jar --help`. I don't use the `gradle run` task because it's harder to pass command line arguments and there seem to be latency problems for standard output as well as weird terminal behaviour when reading from standard input.

## What goes where

### Sources

Sources should be placed in `/src`. The code is currently split up in the following packages:
*   `optic.lua.asm` - intermediate representation of Lua code
*   `optic.lua.codegen` - converting the intermediate representation into some form of output
*   `optic.lua.optimization` - optimizing the intermediate representation
*   `optic.lua.messages` - compiler configuration
*   `optic.lua.io` - the API that is exposed to the users through command line
*   `optic.lua.util` - admit it, every project needs one of these!
*   `optic.lua.runtime` - classes that compiled code depends on at runtime. **Avoid external dependencies in this package**

### Tests
Currently, tests are written in Groovy language (if you're not familiar, it's mostly a superset of Java). This is because Groovy provides an excellent feature called "power assert". Instead of using convoluted matchers, just write your tests using the good old `assert` keyword. When an assertion fails, you'll get an awesome detailed error, which shows the values of all sub-expressions in that statement.

Unit tests should go under `/test`.

Lua code samples, used to test the compiler, are placed in `/samples`.

There are also integration tests which take sample files from `/samples` and verify that the output is correct. Such tests should be placed in `/integration-test`.
