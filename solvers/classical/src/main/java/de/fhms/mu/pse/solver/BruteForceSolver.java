package de.fhms.mu.pse.solver;

import de.fhms.mu.pse.model.primitive.Bitstring;
import lombok.RequiredArgsConstructor;

import java.util.function.Function;

@RequiredArgsConstructor
public class BruteForceSolver {
    public de.fhms.mu.pse.model.solver.SolverResult invoke(int n, Function<Bitstring, Double> costProvider) {
        final var count = (int) Math.pow(2, n);
        final var result = new de.fhms.mu.pse.model.solver.SolverResult(count);
        final var currentSolution = new Bitstring(n);
        var currentCost = Double.MAX_VALUE;

        for (int i = 0; i < count; i++) {
            currentSolution.fill(i);
            currentCost = costProvider.apply(currentSolution);
            result.add(currentSolution, currentCost);
        }

        return result;
    }
}
