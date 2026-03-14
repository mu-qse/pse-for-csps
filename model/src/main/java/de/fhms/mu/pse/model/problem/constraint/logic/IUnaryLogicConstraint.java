package de.fhms.mu.pse.model.problem.constraint.logic;

public interface IUnaryLogicConstraint<T> extends ILogicConstraint<T> {
    boolean eval(T value);
}
