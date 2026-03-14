package de.fhms.mu.pse.model.problem.constraint.logic;

public interface IBinaryLogicConstraint<T> extends ILogicConstraint<T> {
    boolean eval(T a, T b);
}
