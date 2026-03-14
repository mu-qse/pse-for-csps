package de.fhms.mu.pse.examples.sudoku;

import de.fhms.mu.pse.CombinatorialProblemProcessor;
import de.fhms.mu.pse.ProblemSolvingPipeline;
import de.fhms.mu.pse.model.domain.IAggregateFactory;
import de.fhms.mu.pse.solver.SimulatedAnnealingSolver;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static de.fhms.mu.pse.examples.TestUtils.buildSimulatedAnnealingSolver;

public class SudokuQuboTests {
    @Test
    void solve2x2() {
        final var n = 2;
        final var sudokuFactory = new SudokuFactory(n);

        final var problemProcessor = new CombinatorialProblemProcessor<>(Sudoku.class, Cell.class);
        final var solverParams = new SimulatedAnnealingSolver.Params(100000, 1000.0, 0.001,0.9999, 1304L);
        final var solver = buildSimulatedAnnealingSolver(solverParams, Sudoku.class, Cell.class);

        final var result = ProblemSolvingPipeline.create(problemProcessor)
                .from(sudokuFactory)
                .withName(String.format("%dx%d Sudoku", n, n))
                .solve(solver);
        checkResult(result);
    }

    @Test
    @Disabled
    void solve3x3() {
        final var n = 3;
        final var sudokuFactory = new SudokuFactory(n);

        final var problemProcessor = new CombinatorialProblemProcessor<>(Sudoku.class, Cell.class);
        final var solverParams = new SimulatedAnnealingSolver.Params(1000000, 1000.0, 0.001,0.99995, 1304L);
        final var solver = buildSimulatedAnnealingSolver(solverParams, Sudoku.class, Cell.class);

        final var result = ProblemSolvingPipeline.create(problemProcessor)
                .from(sudokuFactory)
                .withName(String.format("%dx%d Sudoku", n, n))
                .solve(solver);
        checkResult(result);
    }

    public static void checkResult(ProblemSolvingPipeline<IAggregateFactory<Sudoku>, Sudoku, Cell>.Result result) {
        final var problem = result.getProblem();

        System.out.println("--- Resulting solutions ---");
        result.getSolutions()
                .forEachOrdered(sudoku -> {
                    System.out.println(sudoku);
                    System.out.println("---");
                    problem.verify(sudoku);
                });
    }
}
