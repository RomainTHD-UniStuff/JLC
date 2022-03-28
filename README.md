# JLC, a Javalette compiler

## How to use

- Run `make` to build the compiler.
- The utility scripts `jlc`, `jlc_riscv`, `jlc_x86` and `jlc_x64` are provided
  to run the compiler. Note that they are only really simple shell scripts that
  run the "real" java compiler behind the scenes.
- The input code is fed through the standard input.
- The output consist, for this first part at least, in an empty standard output
  and a standard error output with either `OK` or `ERROR` with an error message.

## The grammar

Javalette is a C-like / Java-like language with some minor changes.

The shift-reduce conflict warning comes from the infamous dangling else problem.

## Miscellaneous

Please note that, while an optimizer (based on a partial interpreter) and a LLVM
compiler are already implemented, they haven't been formally tested yet. Only
the typechecker is guaranteed to be functional.
