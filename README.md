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
- The input code is fed through the standard input, or a file if JLC is executed
  like `jlc file.jl`.
- The output consist in a standard output containing the assembly and a standard
  error output with either `OK` or `ERROR` with an error message. Otherwise, the
  flag `-o out.ll` can be used.

## Language features

<details><summary>OOP and inheritance, without overload</summary>

Example:

```c
class Counter {
    int val;

    void incr() {
        val++;
    }

    int get() {
        return val;
    }
}

int main () {
  Counter c = new Counter;
  c.incr();
  printInt(c.get());
  return 0;
}
```

</details>

<details><summary>One-dimensional arrays</summary>

Example:

```c
int main() {
    int[] t = new int[10];
    int i = 0;
    while (i < t.length) {
        t[i] = i;
        i++;
    }

    for (int elt : t) {
        printInt(elt);
    }

    return 0;
}
```

</details>

<details><summary>(WIP) Multi-dimensional arrays</summary>

Typechecking done, code generation still in progress.

</details>

## Optimizations implemented

There are several optimizations implemented (click to expand):

<details><summary>Unused functions removal</summary>

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

</details>

<details><summary>Constant propagation</summary>

```c
int main() {
    int n = 24;
    int m;

    if (n % 2 == 0) {
        return 0;
    }

    return 1;
}
```

becomes

```c
int main() {
    int n = 24;
    int m;

    {
        return 0;
    }
}
```

We still need to keep a block around in case some variables are declared inside.
Also, note that here the Condition Simplification optimization is also applied,
see below.

</details>

<details><summary>Pure functions calls removal</summary>

In this optimizer, the notion of purity is slightly different from the
mathematical one. A function is pure here if it does not have side effects and
if it does return eventually.

```c
void foo() {
    int x = 0;
}

void bar() {
    printString("Hello");
}

void baz() {
    baz();
}

int main() {
    foo();
    bar();
    baz();
    return 0;
}
```

becomes

```c
void bar() {
    printString("Hello");
}

void baz() {
    baz();
}

int main() {
    bar();
    baz();
    return 0;
}
```

</details>

<details><summary>Conditions simplification</summary>

Useful with literals evaluation below.

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

Again, the block needs to be kept.

</details>

<details><summary>Literals evaluation</summary>

```c
boolean unknown() {
    return !unknown();
}

int main() {
    if (11 % 2 != 0 && 11 % 3 != 0 && unknown()) {
        printString("11 is not divisible by 2 or 3");
    }
    return 16 / 2 + 3 * 8;
}
```

becomes

```c
boolean unknown() {
    return !unknown();
}

int main() {
    if (unknown()) {
        printString ("11 is not divisible by 2 or 3");
    }
    return 32;
}
```

The mathematical and logical operators are evaluated.

Here, `unknown` is kept because its value cannot safely be resolved at compile
time. It is used in this specific example to avoid condition simplification seen
above, which would make this example "too optimized".

</details>

<details><summary>Dead code elimination</summary>

```c
boolean unknown() {
    return !unknown();
}

int main() {
    if (unknown()) {
        return 0;
    } else {
        return 1;
    }
    printString("Dead code");
}
```

becomes

```c
boolean unknown() {
    return !unknown();
}

int main() {
    if (unknown()) {
        return 0;
    } else {
        return 1;
    }
}
```

</details>

<details><summary>Return checker</summary>

```c
int main() {
    while (true) {
        printString("Infinite loop");
    }

    printString("Unreachable");
    return 0;
}
```

becomes

```c
int main() {
    while (true) {
        printString("Infinite loop");
    }
}
```

More intense optimizations have been implemented but not enabled due to several
tests marked as bad, like `bad032.jl`, `bad034.jl` and others.

```c
int main() { 
    boolean b = false;
    if (b) {
        // No-op
    } else {
        return 0;
    }
}
```

```c
int main() { 
    boolean b = true;
    while (b) {
        return 0;
    }
}
```

</details>

## The grammar

The grammar is based on a Java / C-like language with some minor changes.

There are 2 shift-reduce conflicts:

<details><summary>The infamous dangling else</summary>

Conflict between

```c
Cond. Stmt ::= "if" "(" Expr ")" Stmt ;
```

```c
if (cond) {
  stmt;
}
```

and

```c
CondElse. Stmt ::= "if" "(" Expr ")" Stmt "else" Stmt ;
```

```c
if (cond) {
    stmt1;
} else {
    stmt2;
}
```

</details>

<details><summary>Null and brackets</summary>

Conflict between

```c
ENull. Expr9 ::= "(" Ident ")" "null" ;
```

and

```c
EVar. Expr8 ::= Ident ;
```

</details>
