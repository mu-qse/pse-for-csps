package de.fhms.mu.pse.qubo.penalty;

import de.fhms.mu.pse.model.primitive.Pair;
import de.fhms.mu.pse.model.primitive.matrix.MatrixIndex;
import de.fhms.mu.pse.model.problem.constraint.logic.IUnaryLogicConstraint;
import de.fhms.mu.pse.model.problem.encoding.BinaryVariable;
import de.fhms.mu.pse.model.qubo.IQuboPenalty;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class QuboUnaryLogicPenalty<T> implements IQuboPenalty<T> {
    private final IUnaryLogicConstraint<T> constraint;
    private final double lambda;

    public QuboUnaryLogicPenalty(IUnaryLogicConstraint<T> constraint) {
        this(constraint, -1);
    }

    @Override
    public double getLinearCoefficient(int index, BinaryVariable<T> variable) {
        return this.constraint.eval(variable.getData()) ? this.lambda : 0;
    }

    @Override
    public double getQuadraticCoefficient(MatrixIndex index, Pair<BinaryVariable<T>> binaryVariablePair) {
        return 0;
    }

    @Override
    public double getOffsetValue() {
        return 0;
    }
}
