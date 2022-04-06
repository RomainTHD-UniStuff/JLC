# testing.py

Test-suite for JLC.

All code belongs to the Department of Computer Science and Engineering, Chalmers
University of Technology and Gothenburg University.

## Requirements

Python 3 and `make`.

## Instructions

The test-suite accepts a compressed tar-ball containing the source code. The
tar-ball should be compressed with gzip, bzip2, or xz.

The following command line options are available:

| Option                        | Description                           |
|-------------------------------|---------------------------------------|
| `-h, --help`                  | Show help message.                    |
| `    --llvm`                  | Test the LLVM backend                 |
| `    --x86`                   | Test the 32-bit x86 backend           |
| `    --x64`                   | Test the 64-bit x86 backend           |
| `    --riscv`                 | Test the RISC-V backend               |
| `-x <ext> [ext ...]`          | Test one or more extensions           |
| `    --noclean`               | Do not clean up temporary files       |

If neither of the options `--llvm`, `--x86`, `--x64`, or `--riscv` are present,
only parsing and type checking is tested.

## Extensions

Here is a list of the extensions supported by the testsuite:

| Extension      | Description                                     |
|----------------|-------------------------------------------------|
| arrays1        | Single-dimensional arrays                       |
| arrays2        | Multi-dimensional arrays                        |
| pointers       | Structures and pointers                         |
| objects1       | Objects, first extension                        |
| objects2       | Objects, second extension (method overloading)  |
| adv_structs    | Optional struct tests                           |

## Compiler requirements

### Input / output format

* JLC reads its input from stdin, and write its output (LLVM, or assembly) to
  stdout.
* If it succeeds (no errors), it prints `OK` to stderr, and terminate with exit
  code 0.
* Otherwise, it prints a line containing the word `ERROR` to stderr, and
  terminate with a non-zero exit code.
