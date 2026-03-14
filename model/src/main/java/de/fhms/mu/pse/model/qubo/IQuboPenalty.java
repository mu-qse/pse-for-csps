package de.fhms.mu.pse.model.qubo;

import de.fhms.mu.pse.model.primitive.Pair;
import de.fhms.mu.pse.model.primitive.matrix.MatrixIndex;
import de.fhms.mu.pse.model.problem.encoding.BinaryVariable;

public interface IQuboPenalty<
        T
> {
    double getLinearCoefficient(int index, BinaryVariable<T> variable);
    double getQuadraticCoefficient(MatrixIndex index, Pair<BinaryVariable<T>> variablePair);
    double getOffsetValue();
}
