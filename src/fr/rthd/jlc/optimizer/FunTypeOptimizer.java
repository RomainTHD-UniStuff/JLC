package fr.rthd.jlc.optimizer;

import fr.rthd.jlc.Choice;
import fr.rthd.jlc.env.FunType;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/**
 * Functions optimizer
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
        this._usedBy = new HashSet<>();
        this._purityDependencies = new HashSet<>();
    }

    /**
     * Add a function using this. For example, if `f` is a function that call
     * `g`, then `f` is added to `g`'s usage set.
     * @param caller Function using this
     */
    public void addUsageIn(FunTypeOptimizer caller) {
        this._usedBy.add(caller);
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

    public void updatePurity() {
        Queue<FunTypeOptimizer> queue = new LinkedList<>();
        Set<FunType> visited = new HashSet<>();
        queue.add(this);
        while (!queue.isEmpty()) {
            FunTypeOptimizer funType = queue.poll();
            if (funType.isPure() == Choice.FALSE) {
                this.setPure(Choice.FALSE);
                return;
            } else if (!visited.contains(funType)) {
                visited.add(funType);
                queue.addAll(funType._purityDependencies);
            }
        }
        this.setPure(Choice.TRUE);
    }

    public void clearUsage() {
        this._usedBy.clear();
    }
}
