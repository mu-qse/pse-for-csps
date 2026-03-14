package de.fhms.mu.pse.examples;

import de.fhms.mu.pse.python.BitstringWithCostPlotter;
import de.fhms.mu.pse.python.PythonEnvironment;
import de.fhms.mu.pse.qubo.solver.QuboBruteForceSolverAdapter;
import de.fhms.mu.pse.qubo.solver.QuboPythonProblemSolverAdapter;
import de.fhms.mu.pse.qubo.solver.QuboSimulatedAnnealingSolverAdapter;
import de.fhms.mu.pse.qubo.solver.QuboSolverAdapter;
import de.fhms.mu.pse.solver.MultiThreadedBruteForceSolver;
import de.fhms.mu.pse.solver.SimulatedAnnealingSolver;

import java.io.File;
import java.io.IOException;

public class TestUtils {
    public static <S, E> QuboSolverAdapter<S, E> buildBruteForceSolver(Class<S> aggregateRootType, Class<E> solutionElementType) {
        final var solver = new MultiThreadedBruteForceSolver();
        return new QuboBruteForceSolverAdapter<>(solver);
    }

    public static <S, E> QuboSolverAdapter<S, E> buildSimulatedAnnealingSolver(Class<S> aggregateRootType, Class<E> solutionElementType) {
        final var solverParams = new SimulatedAnnealingSolver.Params(100000, 1000.0, 0.001,0.9995, 1304L);
        return buildSimulatedAnnealingSolver(solverParams, aggregateRootType, solutionElementType);
    }

    public static <S, E> QuboSolverAdapter<S, E> buildSimulatedAnnealingSolver(SimulatedAnnealingSolver.Params solverParams, Class<S> aggregateRootType, Class<E> solutionElementType) {
        final var solver = new SimulatedAnnealingSolver(solverParams);
        return new QuboSimulatedAnnealingSolverAdapter<>(solver);
    }

    public static <S, E> QuboSolverAdapter<S, E> buildPythonSolver(File solverPath, File outputPath, Class<S> aggregateRootType, Class<E> solutionElementType) throws IOException {
        final var pythonEnvironment = new PythonEnvironment(solverPath);
        pythonEnvironment.setup();

        final var scriptPath = new File(solverPath, "solver.py");
        return new QuboPythonProblemSolverAdapter<>(pythonEnvironment, scriptPath, outputPath);
    }

    public static <S, E> QuboSolverAdapter<S, E> addPlotter(QuboSolverAdapter<S, E> solver, String outputName) {
        return solver.addPostProcessor((quboProblem, result) -> {
            final var workingPath = new File(System.getProperty("user.dir"));
            final var bridgePath = new File(workingPath, "../python-bridge");
            final var pythonEnvironment = new PythonEnvironment(bridgePath);

            final var outputPath = new File(workingPath, ".output/" + outputName);
            final var plotter = new BitstringWithCostPlotter(pythonEnvironment);
            plotter.plot(outputName, outputPath, result.getBitstrings());
        });
    }
}
