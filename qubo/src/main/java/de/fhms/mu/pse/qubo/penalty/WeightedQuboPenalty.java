package de.fhms.mu.pse.qubo.penalty;

import de.fhms.mu.pse.model.primitive.Pair;
import de.fhms.mu.pse.model.primitive.matrix.MatrixIndex;
import de.fhms.mu.pse.model.problem.encoding.BinaryVariable;
import de.fhms.mu.pse.model.qubo.IQuboPenalty;
import lombok.Data;

@Data
public class WeightedQuboPenalty<T> implements IQuboPenalty<T> {
    private final Double weight;
    private final IQuboPenalty<T> penalty;

    @Override
    public double getLinearCoefficient(int index, BinaryVariable<T> variable) {
        return this.weight * this.penalty.getLinearCoefficient(index, variable);
    }

    @Override
    public double getQuadraticCoefficient(MatrixIndex index, Pair<BinaryVariable<T>> variablePair) {
        return this.weight * this.penalty.getQuadraticCoefficient(index, variablePair);
    }

    @Override
    public double getOffsetValue() {
        return this.weight * this.penalty.getOffsetValue();
    }
}
