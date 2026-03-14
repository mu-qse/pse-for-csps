package de.fhms.mu.pse.model.problem.constraint;

import de.fhms.mu.pse.model.domain.ElementCombinationTuple;

public interface ISolutionElementConstraint<S, E> extends ICombinatorialProblemConstraint<S, E> {
    ElementCombinationTuple<E> getSolutionElement();
}
