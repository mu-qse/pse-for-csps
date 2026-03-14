package de.fhms.mu.pse.model.qubo;

import de.fhms.mu.pse.model.primitive.Bitstring;
import de.fhms.mu.pse.model.primitive.Pair;
import de.fhms.mu.pse.model.primitive.matrix.MatrixIndex;
import de.fhms.mu.pse.model.problem.encoding.BinaryVariable;
import de.fhms.mu.pse.model.problem.encoding.IProblemEncoding;

import java.util.List;
import java.util.stream.Stream;

public interface IQuboProblemEncoding<
        T
> extends IProblemEncoding<
        BinaryVariable<T>
> {
    Pair<BinaryVariable<T>> getVariablePair(MatrixIndex index);
    Stream<BinaryVariable<T>> getActivatedVariables(Bitstring bitstring);
    Stream<IQuboSolution<T>> getSolutions(List<Bitstring> bitstrings);
    IQuboSolution<T> mapBitstringToSolution(Bitstring bitstring);
}
