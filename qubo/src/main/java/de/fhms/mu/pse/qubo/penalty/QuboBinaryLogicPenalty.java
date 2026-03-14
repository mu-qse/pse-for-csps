package de.fhms.mu.pse.qubo.penalty;

import de.fhms.mu.pse.model.primitive.Pair;
import de.fhms.mu.pse.model.primitive.matrix.MatrixIndex;
import de.fhms.mu.pse.model.problem.constraint.logic.IBinaryLogicConstraint;
import de.fhms.mu.pse.model.problem.encoding.BinaryVariable;
import de.fhms.mu.pse.model.qubo.IQuboPenalty;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class QuboBinaryLogicPenalty<T> implements IQuboPenalty<T> {
    private final IBinaryLogicConstraint<T> constraint;
    private final double lambda;

    public QuboBinaryLogicPenalty(IBinaryLogicConstraint<T> constraint) {
        this(constraint, 2);
    }

    @Override
    public double getLinearCoefficient(int index, BinaryVariable<T> variable) {
        return -1;
    }

    @Override
    public double getQuadraticCoefficient(MatrixIndex index, Pair<BinaryVariable<T>> binaryVariablePair) {
        final var a = binaryVariablePair.getA().getData();
        final var b = binaryVariablePair.getB().getData();
        return !this.constraint.eval(a, b) ? this.lambda : 0;
    }

    @Override
    public double getOffsetValue() {
        return 0;
    }
}
