package fr.rthd.jlc.optimizer;

import fr.rthd.jlc.utils.Choice;
import fr.rthd.jlc.env.FunType;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/**
 * Functions optimizer
 * @author RomainTHD
 * @see FunType
 */
public class FunTypeOptimizer extends FunType {
    /**
     * Set of all functions using this
     */
    private final Set<FunTypeOptimizer> _usedBy;

    /**
     * Set of all functions used, implicitly or not, by this
     */
    private final Set<FunTypeOptimizer> _purityDependencies;

    /**
     * Constructor
     * @param funType Base function
     */
    public FunTypeOptimizer(FunType funType) {
        super(funType);
        _usedBy = new HashSet<>();
        _purityDependencies = new HashSet<>();
    }

    /**
     * Add a function using this. For example, if `f` is a function that call
     * `g`, then `f` is added to `g`'s usage set.
     * @param caller Function using this
     */
    public void addUsageIn(FunTypeOptimizer caller) {
        _usedBy.add(caller);
        caller._purityDependencies.add(this);
    }

    /**
     * Check whether this function is used by the main function or not
     * @return If this function is used by the main function
     */
    public boolean isUsedByMain() {
        Queue<FunTypeOptimizer> queue = new LinkedList<>();
        Set<FunType> visited = new HashSet<>();
        queue.add(this);
        while (!queue.isEmpty()) {
            FunTypeOptimizer funType = queue.poll();
            if (funType.isMain()) {
                return true;
            } else if (!visited.contains(funType)) {
                visited.add(funType);
                queue.addAll(funType._usedBy);
            }
        }
        return false;
    }

    /**
     * Update the purity of this function. A function is pure only if all its
     * steps are pure. Currently, the only impure steps are only function calls
     * to impure functions
     */
    public void updatePurity() {
        Queue<FunTypeOptimizer> queue = new LinkedList<>();
        Set<FunType> visited = new HashSet<>();
        queue.add(this);
        while (!queue.isEmpty()) {
            FunTypeOptimizer funType = queue.poll();
            if (funType.isPure() == Choice.FALSE) {
                setPure(Choice.FALSE);
                return;
            } else if (!visited.contains(funType)) {
                visited.add(funType);
                queue.addAll(funType._purityDependencies);
            }
        }
        setPure(Choice.TRUE);
    }

    public void clearUsage() {
        _usedBy.clear();
    }
}
