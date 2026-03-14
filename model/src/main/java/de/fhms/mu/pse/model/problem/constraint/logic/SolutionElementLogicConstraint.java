package de.fhms.mu.pse.model.problem.constraint.logic;

import de.fhms.mu.pse.model.domain.ElementCombinationTuple;
import de.fhms.mu.pse.model.problem.ICombinatorialProblem;
import de.fhms.mu.pse.model.problem.constraint.ISolutionElementConstraint;
import de.fhms.mu.pse.model.qubo.IQuboPenalty;
import de.fhms.mu.pse.model.qubo.IQuboProblemEncoding;
import de.fhms.mu.pse.model.qubo.interpreter.IQuboConstraintInterpreter;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.function.Predicate;

@Data
@AllArgsConstructor
public class SolutionElementLogicConstraint<S, E> implements IUnaryLogicConstraint<E>, ISolutionElementConstraint<S, E> {
    private final String name;
    private final ElementCombinationTuple<E> solutionElement;
    private final Predicate<E> predicate;
    private double weight;

    public SolutionElementLogicConstraint(ElementCombinationTuple<E> solutionElement, Predicate<E> predicate) {
        this(String.format("Logic constraint for %s elements", solutionElement.getDomainType()), solutionElement, predicate);
    }

    public SolutionElementLogicConstraint(String name, ElementCombinationTuple<E> solutionElement, Predicate<E> predicate) {
        this(name, solutionElement, predicate, 1);
    }

    @Override
    public boolean eval(E value) {
        return this.predicate.test(value);
    }

    @Override
    public boolean isSatisfied(ICombinatorialProblem<S, E> problem, S solution, List<E> elements) {
        for (final var element : elements) {
            if (!this.eval(element)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public IQuboPenalty<ElementCombinationTuple<E>> interpret(IQuboConstraintInterpreter<E> interpreter, IQuboProblemEncoding<ElementCombinationTuple<E>> encoding, ICombinatorialProblem<S, E> problem) {
        return interpreter.interpretConstraint(encoding, problem, this);
    }

    @Override
    public String toString() {
        return this.getName();
    }
}
