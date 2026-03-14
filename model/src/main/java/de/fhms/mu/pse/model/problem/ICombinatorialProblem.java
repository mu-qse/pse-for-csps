package de.fhms.mu.pse.model.problem;

import de.fhms.mu.pse.model.domain.ElementCombinationTuple;
import de.fhms.mu.pse.model.domain.IDomainType;
import de.fhms.mu.pse.model.problem.constraint.ICombinatorialProblemConstraint;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ICombinatorialProblem<S, E> extends IProblem<S> {
    ElementCombinationTuple<E> getSolutionElementPrototype();

    List<CombinatorialProblemElementSet<?>> getSelectableElements();
    Optional<CombinatorialProblemElementSet<?>> getSelectableElements(String name);
    <V> Optional<CombinatorialProblemElementSet<V>> getSelectableElements(Class<V> type);
    <V> Optional<CombinatorialProblemElementSet<V>> getSelectableElements(IDomainType<V> type);
    List<ICombinatorialProblemConstraint<S, E>> getConstraints();

    ICombinatorialProblem<S, E> addSelectableElements(CombinatorialProblemElementSet<?> elementSet);
    ICombinatorialProblem<S, E> addConstraint(ICombinatorialProblemConstraint<S, E> constraint);

    List<ElementCombinationTuple<E>> getPossibleCombinations();

    S createSolution(List<ElementCombinationTuple<E>> combinationTuples);
    E createSolutionElement(ElementCombinationTuple<E> combinationTuple);
}
