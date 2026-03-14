package de.fhms.mu.pse.model.qubo;

import java.util.List;

public interface IQuboSolver<T> {
    List<IQuboSolution<T>> solve(IQuboProblem<IQuboProblemEncoding<T>, T> quboProblem);
}
