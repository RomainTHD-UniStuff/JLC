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

There are 3 shift-reduce conflicts:

- The infamous dangling else
```c
CondElse.  Stmt ::= "if" "(" Expr ")" Stmt "else" Stmt ;
```

Conflict between
```c
if (cond) {
  stmt;
}
```
and
```c
if (cond) {
  stmt1;
} else {
  stmt2;
}
```

- Array types
```c
Array.     Type ::= Type [Dim] ;
```

Conflict between
```c
((int)[])[] t;
```
and
```c
int([][]) t;
```

- Null and brackets
```c
ENull.     Expr8 ::= "(" Ident ")" "null" ;
```
