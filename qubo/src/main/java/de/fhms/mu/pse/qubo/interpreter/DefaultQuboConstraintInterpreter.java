package de.fhms.mu.pse.qubo.interpreter;

import de.fhms.mu.pse.model.domain.ElementCombinationTuple;
import de.fhms.mu.pse.model.problem.ICombinatorialProblem;
import de.fhms.mu.pse.model.problem.constraint.logic.SolutionElementLogicConstraint;
import de.fhms.mu.pse.model.problem.constraint.logic.SolutionElementPairLogicConstraint;
import de.fhms.mu.pse.model.qubo.IQuboPenalty;
import de.fhms.mu.pse.model.qubo.IQuboProblemEncoding;
import de.fhms.mu.pse.model.qubo.interpreter.IQuboConstraintInterpreter;
import de.fhms.mu.pse.qubo.penalty.QuboBinaryLogicPenalty;
import de.fhms.mu.pse.qubo.penalty.QuboUnaryLogicPenalty;

public class DefaultQuboConstraintInterpreter<E> implements IQuboConstraintInterpreter<E> {
    @Override
    public <S> IQuboPenalty<ElementCombinationTuple<E>> interpretConstraint(IQuboProblemEncoding<ElementCombinationTuple<E>> encoding, ICombinatorialProblem<?, E> problem, SolutionElementLogicConstraint<S, E> constraint) {
        return new QuboUnaryLogicPenalty<>(value -> constraint.eval(problem.createSolutionElement(value)));
    }

    @Override
    public <S> IQuboPenalty<ElementCombinationTuple<E>> interpretConstraint(IQuboProblemEncoding<ElementCombinationTuple<E>> encoding, ICombinatorialProblem<?, E> problem, SolutionElementPairLogicConstraint<S, E> constraint) {
        return new QuboBinaryLogicPenalty<>((a, b) -> constraint.eval(problem.createSolutionElement(a), problem.createSolutionElement(b)));
    }
}
