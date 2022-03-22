package fr.rthd.jlc.compiler;

import fr.rthd.jlc.TypeCode;

import java.util.ArrayList;
import java.util.List;

public class Instruction {
    private final List<String> _commands;

    private Instruction() {
        this._commands = new ArrayList<>();
    }

    private Instruction(String command, int stackDifference) {
        this();
        this.add(command, stackDifference);
    }

    public static Instruction comment(String comment) {
        return new Instruction(String.format("; %s", comment), 0);
    }

    public static Instruction dup(TypeCode type) {
        Instruction instruction = new Instruction();
        if (type == TypeCode.CInt || type == TypeCode.CBool) {
            instruction.add("dup", type.getSize());
        } else if (type == TypeCode.CDouble) {
            instruction.add("dup2", type.getSize());
        }

        return instruction;
    }

    public static Instruction loadDefault(TypeCode type) {
        switch (type) {
            case CBool:
                return loadLit(type, false);

            case CDouble:
                return loadLit(type, 0.0);

            case CInt:
                return loadLit(type, 0);

            case CVoid:
            default:
                return noop();
        }
    }

    public static Instruction noop() {
        return new Instruction("nop", 0);
    }

    public static Instruction loadLit(TypeCode type, Object abstractValue) {
        Instruction instruction = new Instruction();
        instruction.add(comment(String.format("load %s lit %s",
                                              type.toString(),
                                              abstractValue.toString()
        )));

        if (type == TypeCode.CInt) {
            int value = (int) abstractValue;
            String instStr;
            if (value == -1) {
                instStr = "iconst_m1";
            } else if (value >= 0 && value <= 5) {
                instStr = String.format("iconst_%d", value);
            } else if (value < Byte.MAX_VALUE) {
                instStr = String.format("bipush %d", value);
            } else if (value < Short.MAX_VALUE) {
                instStr = String.format("sipush %d", value);
            } else {
                instStr = String.format("ldc %d", value);
            }
            instruction.add(instStr, type.getSize());
        } else if (type == TypeCode.CDouble) {
            double value = (double) abstractValue;
            if (value == 0.) {
                instruction.add("dconst_0", type.getSize());
            } else if (value == 1.) {
                instruction.add("dconst_1", type.getSize());
            } else {
                instruction.add(String.format("ldc2_w %f", value), type.getSize());
            }
        } else if (type == TypeCode.CBool) {
            boolean value = (boolean) abstractValue;
            if (value) {
                instruction.add("iconst_1", type.getSize());
            } else {
                instruction.add("iconst_0", type.getSize());
            }
        }

        return instruction;
    }

    public static Instruction storeVar(TypeCode type, int address) {
        Instruction instruction = new Instruction();
        instruction.add(comment(String.format("store %s var", type.toString())));

        if (type == TypeCode.CInt || type == TypeCode.CBool) {
            if (address >= 0 && address <= 3) {
                instruction.add(String.format("istore_%d", address), -type.getSize());
            } else {
                instruction.add(String.format("istore %d", address), -type.getSize());
            }
        } else if (type == TypeCode.CDouble) {
            if (address >= 0 && address <= 3) {
                instruction.add(String.format("dstore_%d", address), -type.getSize());
            } else {
                instruction.add(String.format("dstore %d", address), -type.getSize());
            }
        }

        return instruction;
    }

    public static Instruction loadVar(TypeCode type, int address) {
        Instruction instruction = new Instruction();
        instruction.add(comment(String.format("load %s var", type.toString())));

        if (type == TypeCode.CInt || type == TypeCode.CBool) {
            if (address >= 0 && address <= 3) {
                instruction.add(String.format("iload_%d", address), type.getSize());
            } else {
                instruction.add(String.format("iload %d", address), type.getSize());
            }
        } else if (type == TypeCode.CDouble) {
            if (address >= 0 && address <= 3) {
                instruction.add(String.format("dload_%d", address), type.getSize());
            } else {
                instruction.add(String.format("dload %d", address), type.getSize());
            }
        }

        return instruction;
    }

    public static Instruction declVar(TypeCode type, int address) {
        Instruction instruction = new Instruction();
        instruction.add(loadDefault(type));
        instruction.add(storeVar(type, address));
        return instruction;
    }

    public static Instruction functionCall(String path, List<TypeCode> argsTypes, TypeCode returnType) {
        Instruction instruction = new Instruction();

        StringBuilder call = new StringBuilder();
        call.append("invokestatic ");
        call.append(path);
        call.append("(");

        for (TypeCode arg : argsTypes) {
            call.append(arg.getShortType());
        }

        call.append(")");
        call.append(returnType.getShortType());

        instruction.add(call.toString(), returnType.getSize());
        return instruction;
    }

    public static Instruction label(int address) {
        return new Instruction(String.format("L%d:", address), 0);
    }

    public static Instruction add(TypeCode type) {
        Instruction instruction = new Instruction();
        instruction.add(comment(String.format("add 2 %s values", type.toString())));

        if (type == TypeCode.CInt) {
            instruction.add("iadd", -type.getSize());
        } else if (type == TypeCode.CDouble) {
            instruction.add("dadd", -type.getSize());
        }

        return instruction;
    }

    public static Instruction subtract(TypeCode type) {
        Instruction instruction = new Instruction();
        instruction.add(comment(String.format("subtract 2 %s values", type.toString())));

        if (type == TypeCode.CInt) {
            instruction.add("isub", -type.getSize());
        } else if (type == TypeCode.CDouble) {
            instruction.add("dsub", -type.getSize());
        }

        return instruction;
    }

    public static Instruction multiply(TypeCode type) {
        Instruction instruction = new Instruction();
        instruction.add(comment(String.format("multiply 2 %s values", type.toString())));

        if (type == TypeCode.CInt) {
            instruction.add("imul", -type.getSize());
        } else if (type == TypeCode.CDouble) {
            instruction.add("dmul", -type.getSize());
        }

        return instruction;
    }

    public static Instruction divide(TypeCode type) {
        Instruction instruction = new Instruction();
        instruction.add(comment(String.format("divide 2 %s values", type.toString())));

        if (type == TypeCode.CInt) {
            instruction.add("idiv", -type.getSize());
        } else if (type == TypeCode.CDouble) {
            instruction.add("ddiv", -type.getSize());
        }

        return instruction;
    }

    public static Instruction pop(TypeCode type) {
        Instruction instruction = new Instruction();
        if (type == TypeCode.CInt || type == TypeCode.CBool) {
            instruction.add("pop", -type.getSize());
        } else if (type == TypeCode.CDouble) {
            instruction.add("pop2", -type.getSize());
        }

        return instruction;
    }

    public static Instruction compare(TypeCode type, Comparator comparator, int label) {
        Instruction instruction = new Instruction();
        if (type == TypeCode.CInt || type == TypeCode.CBool) {
            instruction.add(String.format("%s L%d", comparator.getInstName(), label), -2 * type.getSize());
        } else if (type == TypeCode.CDouble) {
            instruction.add("dcmpg", -type.getSize());
            instruction.add(Instruction.loadLit(TypeCode.CInt, comparator.getDoubleComparatorValue()));
            instruction.add(Instruction.compare(TypeCode.CInt, comparator.getDoubleComparator(), label));
        }

        return instruction;
    }

    public static Instruction jumpTo(int label) {
        return new Instruction(String.format("goto L%d", label), 0);
    }

    public static Instruction newLine() {
        return new Instruction("", 0);
    }

    public static Instruction returnInstruction(TypeCode type) {
        switch (type) {
            case CInt:
            case CBool:
                return new Instruction("ireturn", -type.getSize());

            case CDouble:
                return new Instruction("dreturn", -type.getSize());

            case CVoid:
            default:
                return new Instruction("return", -type.getSize());
        }
    }

    private void add(Instruction inst) {
        this._commands.addAll(inst.emit());
    }

    private void add(String command, int stackDifference) {
        this._commands.add(command);
    }

    public List<String> emit() {
        return _commands;
    }
}
