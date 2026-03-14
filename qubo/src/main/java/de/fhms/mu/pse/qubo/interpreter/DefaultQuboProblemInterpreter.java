package de.fhms.mu.pse.qubo.interpreter;

import de.fhms.mu.pse.model.domain.ElementCombinationTuple;
import de.fhms.mu.pse.model.problem.ICombinatorialProblem;
import de.fhms.mu.pse.model.problem.encoding.BinaryVariable;
import de.fhms.mu.pse.model.qubo.IQuboProblem;
import de.fhms.mu.pse.model.qubo.IQuboProblemEncoding;
import de.fhms.mu.pse.model.qubo.interpreter.IQuboConstraintInterpreter;
import de.fhms.mu.pse.model.qubo.interpreter.IQuboProblemInterpreter;
import de.fhms.mu.pse.qubo.QuboProblem;
import de.fhms.mu.pse.qubo.QuboProblemEncoding;
import de.fhms.mu.pse.qubo.penalty.WeightedQuboPenalty;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class DefaultQuboProblemInterpreter<E> implements IQuboProblemInterpreter<E> {
    private final IQuboConstraintInterpreter<E> constraintInterpreter;

    public DefaultQuboProblemInterpreter() {
        this(new DefaultQuboConstraintInterpreter<>());
    }

    @Override
    public <S> IQuboProblem<IQuboProblemEncoding<ElementCombinationTuple<E>>, ElementCombinationTuple<E>> interpretProblem(ICombinatorialProblem<S, E> problem) {
        final var solutionElements = problem.getPossibleCombinations();
        final var encoding = this.createQuboEncoding(solutionElements);

        return new QuboProblem<>(encoding)
                .addAll(this.createQuboPenaltiesFromConstraints(encoding, problem));
    }

    private IQuboProblemEncoding<ElementCombinationTuple<E>> createQuboEncoding(List<ElementCombinationTuple<E>> solutionElements) {
        final var quboProblemEncoding = new QuboProblemEncoding<ElementCombinationTuple<E>>();

        solutionElements.stream()
                .map(solutionElement -> new BinaryVariable<>(solutionElement.toString(), solutionElement))
                .forEach(quboProblemEncoding::add);

        return quboProblemEncoding;
    }

    private <S> List<WeightedQuboPenalty<ElementCombinationTuple<E>>> createQuboPenaltiesFromConstraints(IQuboProblemEncoding<ElementCombinationTuple<E>> encoding, ICombinatorialProblem<S, E> problem) {
        final var constraints = problem.getConstraints();
        return constraints.stream()
                .map(constraint -> {
                    final var weight = constraint.getWeight();
                    final var penalty = constraint.interpret(this.constraintInterpreter, encoding, problem);
                    return new WeightedQuboPenalty<>(weight, penalty);
                })
                .toList();
    }
}
