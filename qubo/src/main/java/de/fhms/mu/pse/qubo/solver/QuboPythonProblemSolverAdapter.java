package de.fhms.mu.pse.qubo.solver;

import com.google.gson.Gson;
import de.fhms.mu.pse.model.domain.ElementCombinationTuple;
import de.fhms.mu.pse.model.primitive.Bitstring;
import de.fhms.mu.pse.model.qubo.IQuboProblem;
import de.fhms.mu.pse.model.qubo.IQuboProblemEncoding;
import de.fhms.mu.pse.model.qubo.QuboMatrix;
import de.fhms.mu.pse.model.solver.SolverResult;
import de.fhms.mu.pse.python.PythonEnvironment;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

public class QuboPythonProblemSolverAdapter<S, E> extends QuboSolverAdapter<S, E> {
    private final PythonEnvironment pythonEnvironment;
    private final File scriptPath;
    private final File outputPath;

    public QuboPythonProblemSolverAdapter(PythonEnvironment pythonEnvironment, File scriptPath, File outputPath) {
        super();

        this.pythonEnvironment = pythonEnvironment;
        this.scriptPath = scriptPath;
        this.outputPath = outputPath;
    }

    @Override
    protected SolverResult invoke(IQuboProblem<IQuboProblemEncoding<ElementCombinationTuple<E>>, ElementCombinationTuple<E>> quboProblem, QuboMatrix quboMatrix) {
        final var outputPath = this.outputPath;
        outputPath.mkdirs();
        System.out.println("Output path: " + outputPath.getAbsolutePath());

        final var quboMatrixFile = new File(outputPath, "qubo_matrix.json");
        final var resultsFile = new File(outputPath, "results.json");

        try {
            final var gson = new Gson();
            final var jsonQuboMatrix = gson.toJson(quboMatrix.toList());
            try (final var fileWriter = new FileWriter(quboMatrixFile)) {
                fileWriter.write(jsonQuboMatrix);
            }

            this.pythonEnvironment.invokeScript(
                    this.scriptPath,
                    quboMatrixFile.getAbsolutePath(),
                    resultsFile.getAbsolutePath(),
                    outputPath.getAbsolutePath()
            );

            final var result = new SolverResult();
            try (final var fileReader = new FileReader(resultsFile)) {
                Arrays.stream(gson.fromJson(fileReader, byte[][].class))
                        .map(Bitstring::new)
                        .forEach(bitstring -> result.add(bitstring, quboMatrix.apply(bitstring)));
            }

            //quboMatrixFile.delete();
            //solutionFile.delete();

            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
