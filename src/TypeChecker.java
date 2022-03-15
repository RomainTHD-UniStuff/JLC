import java.util.LinkedList;

class EnvTypecheck extends Env<TypeCode, FunType> {
    public TypeCode currentFunctionType = null;
}

public class TypeChecker {
    public static boolean canCoerce(TypeCode from, TypeCode to) {
        return from != null && from == to
               || from == TypeCode.CInt && to == TypeCode.CDouble;
    }

    public static TypeCode minType(TypeCode left, TypeCode right) {
        if (left == right) {
            return left;
        } else if (left == TypeCode.CDouble && right == TypeCode.CInt
                   || left == TypeCode.CInt && right == TypeCode.CDouble) {
            return TypeCode.CDouble;
        } else {
            return null;
        }
    }

    public javalette.Absyn.Prog typecheck(javalette.Absyn.Prog p) {
        EnvTypecheck env = new EnvTypecheck();
        p.accept(new ProgVisitorSignature(), env);
        return p.accept(new ProgVisitor(), env);
    }

    public static class ProgVisitorSignature implements javalette.Absyn.Prog.Visitor<Void, EnvTypecheck> {
        public Void visit(javalette.Absyn.Program p, EnvTypecheck env) {
            for (javalette.Absyn.TopDef def : p.listtopdef_) {
                def.accept(new TopDefVisitorSignature(), env);
            }

            env.insertFun("printInt", new FunType(
                TypeCode.CVoid,
                new FunArg(TypeCode.CInt, "i")
            ));
            env.insertFun("printDouble", new FunType(
                TypeCode.CVoid,
                new FunArg(TypeCode.CDouble, "d")
            ));
            env.insertFun("readInt", new FunType(TypeCode.CInt));
            env.insertFun("readDouble", new FunType(TypeCode.CDouble));

            FunType mainFunc = env.lookupFun("main");
            if (mainFunc == null) {
                throw new NoSuchFunctionException("main");
            }

            if (mainFunc.retType != TypeCode.CInt) {
                throw new InvalidReturnedTypeException(
                    TypeCode.CInt.toString(),
                    mainFunc.retType.toString()
                );
            }

            if (!mainFunc.args.isEmpty()) {
                throw new InvalidArgumentCountException(
                    "main",
                    0,
                    mainFunc.args.size()
                );
            }

            return null;
        }
    }

    public static class ProgVisitor implements javalette.Absyn.Prog.Visitor<javalette.Absyn.Prog, EnvTypecheck> {
        public javalette.Absyn.Prog visit(javalette.Absyn.Program p, EnvTypecheck env) {
            /* Code For Program Goes Here */
            for (javalette.Absyn.TopDef x : p.listtopdef_) { /* ... */ }
            return null;
        }
    }

    public static class TopDefVisitor implements javalette.Absyn.TopDef.Visitor<javalette.Absyn.TopDef, EnvTypecheck> {
        public javalette.Absyn.TopDef visit(javalette.Absyn.FnDef p, EnvTypecheck env) {
            /* Code For FnDef Goes Here */
            p.type_.accept(new TypeVisitor(), null);
            //p.ident_;
            for (javalette.Absyn.Arg x : p.listarg_) { /* ... */ }
            p.blk_.accept(new BlkVisitor(), env);
            return null;
        }
    }

    public static class TopDefVisitorSignature implements javalette.Absyn.TopDef.Visitor<Void, EnvTypecheck> {
        public Void visit(javalette.Absyn.FnDef p, EnvTypecheck env) {
            LinkedList<FunArg> argsType = new LinkedList<>();
            for (javalette.Absyn.Arg arg : p.listarg_) {
                argsType.add(arg.accept(new ArgVisitor(), null));
            }

            TypeCode retType = p.type_.accept(new TypeVisitor(), null);
            env.insertFun(p.ident_, new FunType(retType, argsType));

            return null;
        }
    }

    public static class ArgVisitor implements javalette.Absyn.Arg.Visitor<FunArg, Void> {
        public FunArg visit(javalette.Absyn.Argument p, Void ignored) {
            /* Code For Argument Goes Here */
            p.type_.accept(new TypeVisitor(), null);
            //p.ident_;
            return null;
        }
    }

    public static class BlkVisitor implements javalette.Absyn.Blk.Visitor<javalette.Absyn.Blk, EnvTypecheck> {
        public javalette.Absyn.Blk visit(javalette.Absyn.Block p, EnvTypecheck env) {
            /* Code For Block Goes Here */
            for (javalette.Absyn.Stmt x : p.liststmt_) { /* ... */ }
            return null;
        }
    }

    public static class StmtVisitor implements javalette.Absyn.Stmt.Visitor<javalette.Absyn.Stmt, EnvTypecheck> {
        public javalette.Absyn.Stmt visit(javalette.Absyn.Empty p, EnvTypecheck env) {
            /* Code For Empty Goes Here */
            return null;
        }

        public javalette.Absyn.Stmt visit(javalette.Absyn.BStmt p, EnvTypecheck env) {
            /* Code For BStmt Goes Here */
            p.blk_.accept(new BlkVisitor(), env);
            return null;
        }

        public javalette.Absyn.Stmt visit(javalette.Absyn.Decl p, EnvTypecheck env) {
            /* Code For Decl Goes Here */
            p.type_.accept(new TypeVisitor(), null);
            for (javalette.Absyn.Item x : p.listitem_) { /* ... */ }
            return null;
        }

        public javalette.Absyn.Stmt visit(javalette.Absyn.Ass p, EnvTypecheck env) {
            /* Code For Ass Goes Here */
            //p.ident_;
            p.expr_.accept(new ExprVisitor(), env);
            return null;
        }

        public javalette.Absyn.Stmt visit(javalette.Absyn.Incr p, EnvTypecheck env) {
            /* Code For Incr Goes Here */
            //p.ident_;
            return null;
        }

        public javalette.Absyn.Stmt visit(javalette.Absyn.Decr p, EnvTypecheck env) {
            /* Code For Decr Goes Here */
            //p.ident_;
            return null;
        }

        public javalette.Absyn.Stmt visit(javalette.Absyn.Ret p, EnvTypecheck env) {
            /* Code For Ret Goes Here */
            p.expr_.accept(new ExprVisitor(), env);
            return null;
        }

        public javalette.Absyn.Stmt visit(javalette.Absyn.VRet p, EnvTypecheck env) {
            /* Code For VRet Goes Here */
            return null;
        }

        public javalette.Absyn.Stmt visit(javalette.Absyn.Cond p, EnvTypecheck env) {
            /* Code For Cond Goes Here */
            p.expr_.accept(new ExprVisitor(), env);
            p.stmt_.accept(new StmtVisitor(), env);
            return null;
        }

        public javalette.Absyn.Stmt visit(javalette.Absyn.CondElse p, EnvTypecheck env) {
            /* Code For CondElse Goes Here */
            p.expr_.accept(new ExprVisitor(), env);
            p.stmt_1.accept(new StmtVisitor(), env);
            p.stmt_2.accept(new StmtVisitor(), env);
            return null;
        }

        public javalette.Absyn.Stmt visit(javalette.Absyn.While p, EnvTypecheck env) {
            /* Code For While Goes Here */
            p.expr_.accept(new ExprVisitor(), env);
            p.stmt_.accept(new StmtVisitor(), env);
            return null;
        }

        public javalette.Absyn.Stmt visit(javalette.Absyn.SExp p, EnvTypecheck env) {
            /* Code For SExp Goes Here */
            p.expr_.accept(new ExprVisitor(), env);
            return null;
        }
    }

    public static class ItemVisitor implements javalette.Absyn.Item.Visitor<javalette.Absyn.Item, EnvTypecheck> {
        public javalette.Absyn.Item visit(javalette.Absyn.NoInit p, EnvTypecheck env) {
            /* Code For NoInit Goes Here */
            //p.ident_;
            return null;
        }

        public javalette.Absyn.Item visit(javalette.Absyn.Init p, EnvTypecheck env) {
            /* Code For Init Goes Here */
            //p.ident_;
            p.expr_.accept(new ExprVisitor(), env);
            return null;
        }
    }

    public static class TypeVisitor implements javalette.Absyn.Type.Visitor<TypeCode, Void> {
        public TypeCode visit(javalette.Absyn.Bool t, Void ignored) {
            return TypeCode.CBool;
        }

        public TypeCode visit(javalette.Absyn.Int t, Void ignored) {
            return TypeCode.CInt;
        }

        public TypeCode visit(javalette.Absyn.Doub t, Void ignored) {
            return TypeCode.CDouble;
        }

        public TypeCode visit(javalette.Absyn.Void t, Void ignored) {
            return TypeCode.CVoid;
        }

        public TypeCode visit(javalette.Absyn.Fun p, Void ignored) {
            /* Code For Fun Goes Here */
            // p.type_.accept(new TypeVisitor<R, A>(), arg);
            // for (Type x : p.listtype_) { /* ... */ }
            // return null;
            throw new UnsupportedOperationException("visit(javalette.Absyn.Fun)");
        }
    }

    public static class ExprVisitor implements javalette.Absyn.Expr.Visitor<ExpCustom, EnvTypecheck> {
        public ExpCustom visit(javalette.Absyn.EVar p, EnvTypecheck env) {
            /* Code For EVar Goes Here */
            //p.ident_;
            return null;
        }

        public ExpCustom visit(javalette.Absyn.ELitInt p, EnvTypecheck env) {
            /* Code For ELitInt Goes Here */
            //p.integer_;
            return null;
        }

        public ExpCustom visit(javalette.Absyn.ELitDoub p, EnvTypecheck env) {
            /* Code For ELitDoub Goes Here */
            //p.double_;
            return null;
        }

        public ExpCustom visit(javalette.Absyn.ELitTrue p, EnvTypecheck env) {
            /* Code For ELitTrue Goes Here */
            return null;
        }

        public ExpCustom visit(javalette.Absyn.ELitFalse p, EnvTypecheck env) {
            /* Code For ELitFalse Goes Here */
            return null;
        }

        public ExpCustom visit(javalette.Absyn.EApp p, EnvTypecheck env) {
            /* Code For EApp Goes Here */
            //p.ident_;
            for (javalette.Absyn.Expr x : p.listexpr_) { /* ... */ }
            return null;
        }

        public ExpCustom visit(javalette.Absyn.EString p, EnvTypecheck env) {
            /* Code For EString Goes Here */
            //p.string_;
            return null;
        }

        public ExpCustom visit(javalette.Absyn.Neg p, EnvTypecheck env) {
            /* Code For Neg Goes Here */
            p.expr_.accept(new ExprVisitor(), env);
            return null;
        }

        public ExpCustom visit(javalette.Absyn.Not p, EnvTypecheck env) {
            /* Code For Not Goes Here */
            p.expr_.accept(new ExprVisitor(), env);
            return null;
        }

        public ExpCustom visit(javalette.Absyn.EMul p, EnvTypecheck env) {
            /* Code For EMul Goes Here */
            p.expr_1.accept(new ExprVisitor(), env);
            // p.mulop_.accept(new MulOpVisitor<R, A>(), arg);
            p.expr_2.accept(new ExprVisitor(), env);
            return null;
        }

        public ExpCustom visit(javalette.Absyn.EAdd p, EnvTypecheck env) {
            /* Code For EAdd Goes Here */
            p.expr_1.accept(new ExprVisitor(), env);
            // p.addop_.accept(new AddOpVisitor<R, A>(), arg);
            p.expr_2.accept(new ExprVisitor(), env);
            return null;
        }

        public ExpCustom visit(javalette.Absyn.ERel p, EnvTypecheck env) {
            /* Code For ERel Goes Here */
            p.expr_1.accept(new ExprVisitor(), env);
            // p.relop_.accept(new RelOpVisitor<R, A>(), arg);
            p.expr_2.accept(new ExprVisitor(), env);
            return null;
        }

        public ExpCustom visit(javalette.Absyn.EAnd p, EnvTypecheck env) {
            /* Code For EAnd Goes Here */
            p.expr_1.accept(new ExprVisitor(), env);
            p.expr_2.accept(new ExprVisitor(), env);
            return null;
        }

        public ExpCustom visit(javalette.Absyn.EOr p, EnvTypecheck env) {
            /* Code For EOr Goes Here */
            p.expr_1.accept(new ExprVisitor(), env);
            p.expr_2.accept(new ExprVisitor(), env);
            return null;
        }
    }

    public static class AddOpVisitor implements javalette.Absyn.AddOp.Visitor<String, Void> {
        public String visit(javalette.Absyn.Plus p, Void ignored) {
            return "addition";
        }

        public String visit(javalette.Absyn.Minus p, Void ignored) {
            return "subtraction";
        }
    }

    public static class MulOpVisitor implements javalette.Absyn.MulOp.Visitor<String, Void> {
        public String visit(javalette.Absyn.Times p, Void ignored) {
            return "multiplication";
        }

        public String visit(javalette.Absyn.Div p, Void ignored) {
            return "division";
        }

        public String visit(javalette.Absyn.Mod p, Void ignored) {
            return "modulo";
        }
    }

    public static class RelOpVisitor implements javalette.Absyn.RelOp.Visitor<String, TypeCode[]> {
        private boolean sameTypes(TypeCode[] types) {
            TypeCode left = types[0];
            TypeCode right = types[1];
            return canCoerce(minType(left, right), TypeCode.CDouble);
        }

        public String visit(javalette.Absyn.LTH p, TypeCode[] types) {
            return sameTypes(types) ? null : "lower than";
        }

        public String visit(javalette.Absyn.LE p, TypeCode[] types) {
            return sameTypes(types) ? null : "lower or equal";
        }

        public String visit(javalette.Absyn.GTH p, TypeCode[] types) {
            return sameTypes(types) ? null : "greater than";
        }

        public String visit(javalette.Absyn.GE p, TypeCode[] types) {
            return sameTypes(types) ? null : "greater or equal";
        }

        public String visit(javalette.Absyn.EQU p, TypeCode[] types) {
            return sameTypes(types) ? null : "equality";
        }

        public String visit(javalette.Absyn.NE p, TypeCode[] types) {
            return sameTypes(types) ? null : "difference";
        }
    }
}
