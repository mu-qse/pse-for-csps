package de.fhms.mu.pse.model.qubo.interpreter;

import de.fhms.mu.pse.model.domain.ElementCombinationTuple;
import de.fhms.mu.pse.model.problem.ICombinatorialProblem;
import de.fhms.mu.pse.model.problem.constraint.logic.SolutionElementLogicConstraint;
import de.fhms.mu.pse.model.problem.constraint.logic.SolutionElementPairLogicConstraint;
import de.fhms.mu.pse.model.qubo.IQuboPenalty;
import de.fhms.mu.pse.model.qubo.IQuboProblemEncoding;

public interface IQuboConstraintInterpreter<E> {
    <S> IQuboPenalty<ElementCombinationTuple<E>> interpretConstraint(IQuboProblemEncoding<ElementCombinationTuple<E>> encoding, ICombinatorialProblem<?, E> problem, SolutionElementLogicConstraint<S, E> constraint);
    <S> IQuboPenalty<ElementCombinationTuple<E>> interpretConstraint(IQuboProblemEncoding<ElementCombinationTuple<E>> encoding, ICombinatorialProblem<?, E> problem, SolutionElementPairLogicConstraint<S, E> constraint);
}
