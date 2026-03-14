package de.fhms.mu.pse.model.qubo.interpreter;

import de.fhms.mu.pse.model.domain.ElementCombinationTuple;
import de.fhms.mu.pse.model.problem.ICombinatorialProblem;
import de.fhms.mu.pse.model.qubo.IQuboProblem;
import de.fhms.mu.pse.model.qubo.IQuboProblemEncoding;

public interface IQuboProblemInterpreter<E> {
    <S> IQuboProblem<IQuboProblemEncoding<ElementCombinationTuple<E>>, ElementCombinationTuple<E>> interpretProblem(ICombinatorialProblem<S, E> problem);
}
