package de.fhms.mu.pse.examples.sudoku;

import de.fhms.mu.pse.CombinatorialProblemProcessor;
import de.fhms.mu.pse.ProblemSolvingPipeline;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static de.fhms.mu.pse.examples.TestUtils.buildPythonSolver;
import static de.fhms.mu.pse.examples.sudoku.SudokuQuboTests.checkResult;

@Disabled
public class SudokuPythonTests {
    @Test
    public void testQaoaSolver() throws IOException {
        final var n = 2;
        final var sudokuFactory = new SudokuFactory(n);

        final var problemProcessor = new CombinatorialProblemProcessor<>(Sudoku.class, Cell.class);
        final var workingPath = new File(System.getProperty("user.dir"));
        final var solverPath = new File(workingPath, "../solvers/quantum/qaoa");
        final var outputPath = new File(workingPath, "../solvers/quantum/.output/sudoku_qaoa");
        final var solver = buildPythonSolver(solverPath, outputPath, Sudoku.class, Cell.class);

        final var result = ProblemSolvingPipeline.create(problemProcessor)
                .from(sudokuFactory)
                .withName(String.format("%dx%d Sudoku", n, n))
                .solve(solver);
        checkResult(result);
    }

    @Test
    public void testQuantumAnnealingSolver() throws IOException {
        final var n = 2;
        final var sudokuFactory = new SudokuFactory(n);

        final var problemProcessor = new CombinatorialProblemProcessor<>(Sudoku.class, Cell.class);
        final var workingPath = new File(System.getProperty("user.dir"));
        final var solverPath = new File(workingPath, "../solvers/quantum/quantum-annealing");
        final var outputPath = new File(workingPath, "../solvers/quantum/.output/sudoku_quantum-annealing");
        final var solver = buildPythonSolver(solverPath, outputPath, Sudoku.class, Cell.class);

        final var result = ProblemSolvingPipeline.create(problemProcessor)
                .from(sudokuFactory)
                .withName(String.format("%dx%d Sudoku", n, n))
                .solve(solver);
        checkResult(result);
    }
}
