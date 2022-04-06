# JLC, a Java-like compiler

## Requirements

- Make
- Java
- BNFC
- Clang
- LLVM

## How to use

- Run `make` to build the compiler.
- The utility scripts `jlc`, `jlc_riscv`, `jlc_x86` and `jlc_x64` are provided
  to run the compiler. Note that they are only really simple shell scripts that
  run the "real" JLC compiler behind the scenes.
- The input code is fed through the standard input.
- The output consist in an empty standard output and a standard error output
  with either `OK` or `ERROR` with an error message.

## The grammar

The grammar is based on a Java / C-like language with some minor changes.

The shift-reduce conflict warning comes from the infamous dangling else problem.
