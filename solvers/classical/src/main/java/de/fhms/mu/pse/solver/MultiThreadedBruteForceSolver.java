package de.fhms.mu.pse.solver;

import de.fhms.mu.pse.model.primitive.Bitstring;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@RequiredArgsConstructor
public class MultiThreadedBruteForceSolver extends BruteForceSolver {
    @Override
    public de.fhms.mu.pse.model.solver.SolverResult invoke(int n, Function<Bitstring, Double> costProvider) {
        final var count = (long) Math.pow(2, n);
        System.out.println("Enumerations: " + count);

        final var threadsCount = Runtime.getRuntime().availableProcessors();
        final var countPerJob = (long) Math.ceil((double) count / threadsCount);
        final var jobThreads = this.createJobThreads(n, costProvider, count, countPerJob);
        System.out.printf("Job threads: %d, %d per thread%n", jobThreads.size(), countPerJob);

        for (final var jobThread : jobThreads) {
            //System.out.printf("\t- %s (%d, %d)%n", jobThread.getThread().getName(), jobThread.getStart(), jobThread.getEnd());
            jobThread.start();
        }

        return jobThreads.stream()
                .map(JobThread::waitForCompletion)
                .reduce(new de.fhms.mu.pse.model.solver.SolverResult(count), de.fhms.mu.pse.model.solver.SolverResult::add);
    }

    private List<JobThread> createJobThreads(int n, Function<Bitstring, Double> costProvider, long count, long countPerJob) {
        final var jobThreads = new ArrayList<JobThread>();
        for (var start = 0L; start < count; start += countPerJob) {
            final var startSolution = new Bitstring(n);
            final var end = Math.min(start + countPerJob, count);
            final var jobThread = new JobThread(startSolution, start, end, costProvider);
            jobThreads.add(jobThread);
        }
        return jobThreads;
    }

    @Getter
    static class JobThread {
        private final Bitstring startSolution;
        private final long start;
        private final long end;
        private final Function<Bitstring, Double> costProvider;
        private final de.fhms.mu.pse.model.solver.SolverResult result = new de.fhms.mu.pse.model.solver.SolverResult();
        private final Thread thread = new Thread(this::run);

        public JobThread(Bitstring startSolution, long start, long end, Function<Bitstring, Double> costProvider) {
            this.startSolution = startSolution;
            this.start = start;
            this.end = end;
            this.costProvider = costProvider;
        }

        public void start() {
            this.thread.start();
        }

        public de.fhms.mu.pse.model.solver.SolverResult waitForCompletion() {
            try {
                this.thread.join();
            } catch (InterruptedException e) {
                System.err.print(e.getMessage());
            }
            return this.result;
        }

        protected void run() {
            final var currentSolution = this.startSolution;
            var currentCost = Double.MAX_VALUE;

            for (var i = this.start; i < this.end; i++) {
                currentSolution.fill(i);
                currentCost = this.costProvider.apply(currentSolution);
                this.result.add(currentSolution, currentCost);
            }
        }
    }
}
