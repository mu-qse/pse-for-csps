package de.fhms.mu.pse.qubo.solver;

import de.fhms.mu.pse.model.domain.ElementCombinationTuple;
import de.fhms.mu.pse.model.primitive.BitstringWithCost;
import de.fhms.mu.pse.model.problem.ICombinatorialProblem;
import de.fhms.mu.pse.model.qubo.*;
import de.fhms.mu.pse.model.qubo.interpreter.IQuboProblemInterpreter;
import de.fhms.mu.pse.model.solver.ProblemSolverAdapter;
import de.fhms.mu.pse.model.solver.SolverResult;
import de.fhms.mu.pse.qubo.interpreter.DefaultQuboProblemInterpreter;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@RequiredArgsConstructor
public abstract class QuboSolverAdapter<S, E> extends ProblemSolverAdapter<S, E> implements IQuboSolver<ElementCombinationTuple<E>> {
    private final IQuboProblemInterpreter problemInterpreter;
    private final List<PostProcessor> postProcessors = new ArrayList<>();

    public QuboSolverAdapter() {
        this(new DefaultQuboProblemInterpreter());
    }

    public QuboSolverAdapter<S, E> addPostProcessor(PostProcessor postProcessor) {
        this.postProcessors.add(postProcessor);
        return this;
    }

    @Override
    protected Stream<S> invoke(ICombinatorialProblem<S, E> problem) {
        final var quboProblem = this.problemInterpreter.interpretProblem(problem);
        final var quboSolutions = this.solve(quboProblem);
        if (quboSolutions.isEmpty()) {
            throw new IllegalStateException("No solution found.");
        }

        return this.mapSolutions(problem, quboSolutions);
    }

    @Override
    public List<IQuboSolution<ElementCombinationTuple<E>>> solve(IQuboProblem<IQuboProblemEncoding<ElementCombinationTuple<E>>, ElementCombinationTuple<E>> quboProblem) {
        final var encoding = quboProblem.getEncoding();
        System.out.println("Encoding: " + encoding.getName());

        final var quboMatrix = quboProblem.buildQuboMatrix();
        System.out.printf("QUBO matrix: %d x %d (%d)%n", quboMatrix.length(), quboMatrix.length(), quboMatrix.entriesCount());
        System.out.println(quboMatrix);
        final var offsetValue = quboProblem.getOffsetValue();
        System.out.printf("Offset: %.3f %n", offsetValue);

        final var result = this.invoke(quboProblem, quboMatrix);

        System.out.printf("Found bitstrings: %d%n", result.getSolutionBitstringsCount());
        System.out.println(IntStream.range(0, result.getSolutionBitstringsCount())
                .mapToObj(i -> {
                    final var bitstringWithCost = result.getSolutionBitstring(i);
                    final var bitstring = bitstringWithCost.getBitstring();
                    return String.format("%d, %d: %s\n\t=> %s\n\t=> %.2f", i, bitstring.getNumericValue(), bitstring, encoding.mapBitstringToSolution(bitstring), bitstringWithCost.getCost() + offsetValue);
                })
                .collect(Collectors.joining("\n")));

        this.postProcess(quboProblem, result);

        final var solutionBitstrings = result.getSolutionBitstrings()
                .map(BitstringWithCost::getBitstring)
                .toList();
        return encoding.getSolutions(solutionBitstrings).toList();
    }

    private void postProcess(IQuboProblem<IQuboProblemEncoding<ElementCombinationTuple<E>>, ElementCombinationTuple<E>> quboProblem, SolverResult result) {
        final var postProcessorThreads = this.postProcessors.stream()
                .map(postProcessor -> new Thread(() -> {
                    try {
                        postProcessor.postProcess(quboProblem, result);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }))
                .peek(Thread::start)
                .toList();

        for (final var thread : postProcessorThreads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    private Stream<S> mapSolutions(ICombinatorialProblem<S, E> problem, List<IQuboSolution<ElementCombinationTuple<E>>> quboSolutions) {
        return quboSolutions.stream()
                .map(solution -> solution.getElements().toList())
                .map(problem::createSolution);
    }

    protected abstract SolverResult invoke(IQuboProblem<IQuboProblemEncoding<ElementCombinationTuple<E>>, ElementCombinationTuple<E>> quboProblem, QuboMatrix quboMatrix);

    public interface PostProcessor {
        void postProcess(IQuboProblem<?, ?> quboProblem, SolverResult result) throws IOException;
    }
}
