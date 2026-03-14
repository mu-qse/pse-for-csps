package de.fhms.mu.pse.model.solver;

import de.fhms.mu.pse.model.problem.ICombinatorialProblem;

import java.time.Duration;
import java.time.Instant;
import java.util.stream.Stream;

public abstract class ProblemSolverAdapter<S, E> implements IProblemSolverAdapter<S, E> {
    @Override
    public Stream<S> solve(ICombinatorialProblem<S, E> problem) {
        System.out.println("---");
        System.out.println(problem);
        System.out.println("---");
        System.out.println("Solver: " + this.getClass().getSimpleName());

        final var startTimestamp = Instant.now();
        System.out.printf("Start timestamp: %s%n", startTimestamp);

        final var result = this.invoke(problem);

        final var endTimestamp = Instant.now();
        final var deltaTime = Duration.between(startTimestamp, endTimestamp);
        System.out.printf("End timestamp: %s%n", endTimestamp);
        System.out.printf("Execution time: %s%n", SolverUtils.formatDeltaTime(deltaTime));
        System.out.println("---");

        return result;
    }

    protected abstract Stream<S> invoke(ICombinatorialProblem<S, E> problem);
}
