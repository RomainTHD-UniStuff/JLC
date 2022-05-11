package fr.rthd.jlc.optimizer;

import fr.rthd.jlc.AnnotatedExpr;
import fr.rthd.jlc.Visitor;
import fr.rthd.jlc.env.ClassType;
import fr.rthd.jlc.env.Env;
import fr.rthd.jlc.env.FunType;
import javalette.Absyn.ELitDoub;
import javalette.Absyn.ELitFalse;
import javalette.Absyn.ELitInt;
import javalette.Absyn.ELitTrue;
import javalette.Absyn.EString;
import javalette.Absyn.Expr;
import javalette.Absyn.Prog;
import org.jetbrains.annotations.NotNull;

/**
 * Optimizer
 *
 * - Unused functions removal, even with cycles or recursive calls
 * - Constants propagation
 * - Pure functions calls removal
 * - Simplification of if and while according to their condition
 * - Literals evaluation
 * - Dead code elimination
 * - Return checker
 * @author RomainTHD
 */
public class Optimizer implements Visitor {
    public Optimizer(int optimizationLevel) {
    }

    /**
     * Generic action on an operator. The correct function will be called if
     * both sides are literals. Otherwise, the default action will be called.
     * @param left Left expression
     * @param right Right expression
     * @param onInt Function for integer operations
     * @param onDouble Function for double operations
     * @param onBool Function for boolean operations
     * @param onDefault Default action if the expressions are not literals
     * @return Result of the operation, according to the correct function call
     */
    static Expr operatorAction(
        AnnotatedExpr<?> left,
        AnnotatedExpr<?> right,
        OperatorAction<Integer> onInt,
        OperatorAction<Double> onDouble,
        OperatorAction<Boolean> onBool,
        OperatorAction<Expr> onDefault
    ) {
        if (left.getParentExp() instanceof ELitInt &&
            right.getParentExp() instanceof ELitInt) {
            int lvalue = ((ELitInt) left.getParentExp()).integer_;
            int rvalue = ((ELitInt) right.getParentExp()).integer_;
            return onInt.execute(lvalue, rvalue);
        } else if (left.getParentExp() instanceof ELitDoub &&
                   right.getParentExp() instanceof ELitDoub) {
            double lvalue = ((ELitDoub) left.getParentExp()).double_;
            double rvalue = ((ELitDoub) right.getParentExp()).double_;
            return onDouble.execute(lvalue, rvalue);
        } else {
            Boolean lvalue = null;
            Boolean rvalue = null;

            if (left.getParentExp() instanceof ELitTrue) {
                lvalue = true;
            } else if (left.getParentExp() instanceof ELitFalse) {
                lvalue = false;
            }

            if (right.getParentExp() instanceof ELitTrue) {
                rvalue = true;
            } else if (right.getParentExp() instanceof ELitFalse) {
                rvalue = false;
            }

            if (lvalue == null || rvalue == null) {
                return onDefault.execute(left, right);
            } else {
                return onBool.execute(lvalue, rvalue);
            }
        }
    }

    /**
     * @param exp Expression
     * @return If the expression is a literal or not
     */
    static boolean isLiteral(AnnotatedExpr<?> exp) {
        return exp.getParentExp() instanceof ELitInt
               || exp.getParentExp() instanceof ELitDoub
               || exp.getParentExp() instanceof EString
               || exp.getParentExp() instanceof ELitTrue
               || exp.getParentExp() instanceof ELitFalse;
    }

    @NotNull
    @Override
    public Prog accept(
        @NotNull Prog p,
        @NotNull Env<?, FunType, ClassType<?>> parentEnv
    ) {
        EnvOptimizer env = new EnvOptimizer(parentEnv);
        // First pass will mark functions as pure or impure
        p = p.accept(new ProgVisitor(), env);
        env.newPass();
        // Second pass will optimize expressions based on functions purity
        return p.accept(new ProgVisitor(), env);
    }
}
