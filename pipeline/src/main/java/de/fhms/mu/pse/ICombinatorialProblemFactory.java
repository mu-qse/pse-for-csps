package de.fhms.mu.pse;

import de.fhms.mu.pse.model.problem.ICombinatorialProblem;

public interface ICombinatorialProblemFactory<I, S, E> {
    ICombinatorialProblem<S, E> build(String name, I input);
}
