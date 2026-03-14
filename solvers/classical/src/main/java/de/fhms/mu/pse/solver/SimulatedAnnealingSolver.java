package de.fhms.mu.pse.solver;

import de.fhms.mu.pse.model.primitive.Bitstring;
import lombok.RequiredArgsConstructor;

import java.util.Random;
import java.util.function.Function;

@RequiredArgsConstructor
public class SimulatedAnnealingSolver {
    private final Params params;

    public de.fhms.mu.pse.model.solver.SolverResult invoke(int n, Function<Bitstring, Double> costProvider) {
        final var random = this.params.seed != null ? new Random(this.params.seed) : new Random();

        final var result = new de.fhms.mu.pse.model.solver.SolverResult();
        var currentSolution = new Bitstring(n, random); // Initialize solution randomly
        var currentCost = costProvider.apply(currentSolution);
        var candidateSolution = new Bitstring(n);

        int iteration;
        var temp = this.params.initialTemp();

        double candidateCost;
        double deltaCost;

        for (iteration = 0; iteration < this.params.maxIterations(); iteration++) {
            // Pick a random bit to flip
            candidateSolution.fill(currentSolution);
            candidateSolution.flip(random.nextInt(n)); // Flip 0 <-> 1

            candidateCost = costProvider.apply(candidateSolution);
            deltaCost = candidateCost - currentCost;

            // Acceptance criteria
            if (deltaCost < 0 || Math.exp(-deltaCost / temp) > random.nextDouble()) {
                currentSolution.fill(candidateSolution);
                currentCost = candidateCost;

                result.add(currentSolution, currentCost);
            } else {
                result.add(candidateSolution, candidateCost);
            }

            // Cool down
            temp *= this.params.coolingRate();

            if (temp < this.params.minTemp()) {
                break;
            }
        }

        System.out.printf("Final temperature: %.3f%n", temp);
        System.out.printf("Iterations: %d%n", iteration);
        return result;
    }

    public record Params(int maxIterations, double initialTemp, double minTemp, double coolingRate, Long seed) {

    }
}
