package de.fhms.mu.pse;

import de.fhms.mu.pse.model.problem.ICombinatorialProblem;
import de.fhms.mu.pse.model.solver.IProblemSolverAdapter;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ProblemSolvingPipeline<I, S, E> {
    private final ICombinatorialProblemFactory<I, S, E> problemBuilder;

    public BuildScope from(I input) {
        return new BuildScope(input);
    }

    public static <I, S, E> ProblemSolvingPipeline<I, S, E> create(ICombinatorialProblemFactory<I, S, E> problemBuilder) {
        return new ProblemSolvingPipeline<>(problemBuilder);
    }

    @Data
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public class BuildScope {
        private final I input;
        private String name;

        public BuildScope(I input) {
            this(input, UUID.randomUUID().toString());
        }

        public BuildScope withName(String name) {
            this.name = name;
            return this;
        }

        public BuildScope peek(Consumer<ICombinatorialProblem<S, E>> consumer) {
            final var problem = ProblemSolvingPipeline.this.problemBuilder.build(name, input);
            consumer.accept(problem);
            return this;
        }

        public Result solve(IProblemSolverAdapter<S, E> solverAdapter) {
            final var problem = ProblemSolvingPipeline.this.problemBuilder.build(name, input);
            final var solutions = solverAdapter.solve(problem);
            return new Result(problem, solutions);
        }
    }

    @Data
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public class Result {
        private final ICombinatorialProblem<S, E> problem;
        private final Stream<S> solutions;
    }
}
