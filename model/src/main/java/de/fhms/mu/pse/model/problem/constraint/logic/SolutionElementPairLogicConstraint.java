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
import java.util.function.BiPredicate;

@Data
@AllArgsConstructor
public class SolutionElementPairLogicConstraint<S, E> implements IBinaryLogicConstraint<E>, ISolutionElementConstraint<S, E> {
    private final String name;
    private final ElementCombinationTuple<E> solutionElement;
    private final BiPredicate<E, E> predicate;
    private double weight;

    public SolutionElementPairLogicConstraint(ElementCombinationTuple<E> solutionElement, BiPredicate<E, E> predicate) {
        this(String.format("Logic constraint for %s element pairs", solutionElement.getDomainType()), solutionElement, predicate, 1);
    }

    public SolutionElementPairLogicConstraint(String name, ElementCombinationTuple<E> solutionElement, BiPredicate<E, E> predicate) {
        this(name, solutionElement, predicate, 1);
    }

    @Override
    public boolean eval(E a, E b) {
        return this.predicate.test(a, b);
    }

    @Override
    public boolean isSatisfied(ICombinatorialProblem<S, E> problem, S solution, List<E> elements) {
        for (var i = 0; i < elements.size(); i++) {
            for (var j = 0; j < elements.size(); j++) {
                if (i == j) {
                    continue;
                }

                if (!this.eval(elements.get(i), elements.get(j))) {
                    return false;
                }
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
