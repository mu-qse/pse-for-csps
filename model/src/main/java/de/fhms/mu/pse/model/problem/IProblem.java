package de.fhms.mu.pse.model.problem;

import de.fhms.mu.pse.model.domain.IAggregateRoot;
import de.fhms.mu.pse.model.domain.IProblemDomain;
import de.fhms.mu.pse.model.problem.constraint.ConstraintViolationException;

public interface IProblem<S> {
    String getName();
    IProblemDomain<S> getDomain();
    IAggregateRoot<S> getSolutionAggregateRoot();

    boolean isFeasible(S solution);
    void verify(S solution) throws ConstraintViolationException;
}
