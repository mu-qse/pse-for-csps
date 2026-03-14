package de.fhms.mu.pse.qubo.solver;

import de.fhms.mu.pse.model.domain.ElementCombinationTuple;
import de.fhms.mu.pse.model.qubo.IQuboProblem;
import de.fhms.mu.pse.model.qubo.IQuboProblemEncoding;
import de.fhms.mu.pse.model.qubo.QuboMatrix;
import de.fhms.mu.pse.model.solver.SolverResult;
import de.fhms.mu.pse.solver.BruteForceSolver;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class QuboBruteForceSolverAdapter<S, E> extends QuboSolverAdapter<S, E> {
    private final BruteForceSolver solver;

    @Override
    protected SolverResult invoke(IQuboProblem<IQuboProblemEncoding<ElementCombinationTuple<E>>, ElementCombinationTuple<E>> quboProblem, QuboMatrix quboMatrix) {
        final var n = quboMatrix.length();
        return this.solver.invoke(n, quboMatrix::apply);
    }
}
