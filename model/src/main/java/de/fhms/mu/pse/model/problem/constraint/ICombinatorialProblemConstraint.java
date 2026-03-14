package de.fhms.mu.pse.model.problem.constraint;

import de.fhms.mu.pse.model.domain.ElementCombinationTuple;
import de.fhms.mu.pse.model.problem.ICombinatorialProblem;
import de.fhms.mu.pse.model.qubo.IQuboPenalty;
import de.fhms.mu.pse.model.qubo.IQuboProblemEncoding;
import de.fhms.mu.pse.model.qubo.interpreter.IQuboConstraintInterpreter;

import java.util.List;

public interface ICombinatorialProblemConstraint<S, E> extends IProblemConstraint<ICombinatorialProblem<S, E>, S> {
    boolean isSatisfied(ICombinatorialProblem<S, E> problem, S solution, List<E> elements);
    IQuboPenalty<ElementCombinationTuple<E>> interpret(IQuboConstraintInterpreter<E> interpreter, IQuboProblemEncoding<ElementCombinationTuple<E>> encoding, ICombinatorialProblem<S, E> problem);
}
