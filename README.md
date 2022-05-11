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

## Optimizations implemented

There are several optimizations implemented:

### Unused functions removal

```c
void foo() {
    foo();
}

void bar() {
    baz();
}

void baz() {
    bar();
}

int main() {
    return 0;
}
```

becomes

```c
int main() {
    return 0;
}
```

### Constant propagation

```c
int main() {
    int n = 24;
    if (n % 2 == 0) {
        n++;
    }

    if (n % 5 == 0) {
        return 0;
    }

    return 1;
}
```

becomes

```c
int main() {
    int n = 24;

    {
        n++;
    }

    {
        return 0;
    }
}
```

### Pure functions calls removal

Note that, in this optimizer, the notion of purity is slightly different from
the mathematical one. A function is pure here if it does not have side effects
and if it does return eventually.

```c
void foo() {
    int x = 0;
}

void bar() {
    printString("Hello");
}

int main() {
    foo();
    bar();
    return 0;
}
```

becomes

```c
void bar() {
    printString("Hello");
}

int main() {
    bar();
    return 0;
}
```

### Conditions simplification

Used with literals evaluation below.

```c
int main() {
    if (true) {
        return 0;
    } else {
        return 1;
    }
}
```

becomes

```c
int main() {
    {
        return 0;
    }
}
```

We still need to keep a block around the condition body in case some variables
are declared inside.

### Literals evaluation

```c
int sideEffect() {
    printString("Side effect");
    return 0;
}

int main() {
    if (11 % 2 != 0 && 11 % 3 != 0 && sideEffect() == 0) {
        printString("11 is not divisible by 2 or 3");
    }
    return 16 / 2 + 3 * 8;
}
```

becomes

```c
int sideEffect() {
    printString ("Side effect");
    return 0;
}

int main() {
    if (sideEffect() == 0) {
        printString ("11 is odd");
    }
    return 32;
}
```

### Dead code elimination

```c
int main() {
    return 0;
    printString("Dead code");
}
```

becomes

```c
int main() {
    return 0;
}
```

### Return checker

```c
int sideEffect() {
    printString("Side effect");
    return 0;
}

int main() {
    if (sideEffect() == 0) {
        return 0;
    } else {
        while (true) {
            printString("Infinite loop");
        }
    }

    printString("Unreachable");
}
```

becomes

```c
int sideEffect() {
    printString("Side effect");
    return 0;
}

int main() {
    if (sideEffect() == 0) {
        return 0;
    } else {
        while (true) {
            printString("Infinite loop");
        }
    }
}
```

## The grammar

The grammar is based on a Java / C-like language with some minor changes.

There are 3 shift-reduce conflicts:

### The infamous dangling else

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

### Array types

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

### Null and brackets

```c
ENull.     Expr8 ::= "(" Ident ")" "null" ;
```
