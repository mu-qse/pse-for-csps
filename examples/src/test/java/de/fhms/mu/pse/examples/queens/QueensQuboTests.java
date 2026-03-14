package de.fhms.mu.pse.examples.queens;

import de.fhms.mu.pse.CombinatorialProblemProcessor;
import de.fhms.mu.pse.ProblemSolvingPipeline;
import de.fhms.mu.pse.model.domain.IAggregateFactory;
import de.fhms.mu.pse.solver.SimulatedAnnealingSolver;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static de.fhms.mu.pse.examples.TestUtils.buildSimulatedAnnealingSolver;

public class QueensQuboTests {
    @ParameterizedTest
    @ValueSource(ints  = { 4, 5, 6, 7, 8 })
    void solveSmallN(int n) {
        final var chessboardFactory = new ChessboardFactory(n);

        final var problemProcessor = new CombinatorialProblemProcessor<>(Chessboard.class, Queen.class);
        final var solverParams = new SimulatedAnnealingSolver.Params(200000, 1000.0, 0.001, 0.99995, 1304L);
        final var solver = buildSimulatedAnnealingSolver(solverParams, Chessboard.class, Queen.class);

        final var result = ProblemSolvingPipeline.create(problemProcessor)
                .from(chessboardFactory)
                .withName(String.format("%d-queens", n))
                .solve(solver);
        checkResult(result);
    }

    @ParameterizedTest
    @ValueSource(ints  = { 9, 10, 11, 12, 13, 14 })
    void solveLargeN(int n) {
        final var chessboardFactory = new ChessboardFactory(n);

        final var problemProcessor = new CombinatorialProblemProcessor<>(Chessboard.class, Queen.class);
        final var solverParams = new SimulatedAnnealingSolver.Params(1000000, 1000.0, 0.001, 0.99999, 1304L);
        final var solver = buildSimulatedAnnealingSolver(solverParams, Chessboard.class, Queen.class);

        final var result = ProblemSolvingPipeline.create(problemProcessor)
                .from(chessboardFactory)
                .withName(String.format("%d-queens", n))
                .solve(solver);
        checkResult(result);
    }

    public static void checkResult(ProblemSolvingPipeline<IAggregateFactory<Chessboard>, Chessboard, Queen>.Result result) {
        final var problem = result.getProblem();

        System.out.println("--- Resulting solutions ---");
        result.getSolutions()
                .forEachOrdered(chessboard -> {
                    System.out.println(chessboard);
                    System.out.println("---");
                    problem.verify(chessboard);
                });
    }
}
