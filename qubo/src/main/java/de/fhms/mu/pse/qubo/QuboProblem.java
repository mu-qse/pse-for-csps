package de.fhms.mu.pse.qubo;

import de.fhms.mu.pse.model.qubo.IQuboPenalty;
import de.fhms.mu.pse.model.qubo.IQuboProblem;
import de.fhms.mu.pse.model.qubo.IQuboProblemEncoding;
import de.fhms.mu.pse.model.qubo.QuboMatrix;
import de.fhms.mu.pse.qubo.penalty.WeightedQuboPenalty;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class QuboProblem<
        TEncoding extends IQuboProblemEncoding<T>,
        T
> implements IQuboProblem<
        TEncoding,
        T
> {
    private final TEncoding encoding;
    private final List<WeightedQuboPenalty<T>> quboPenalties;

    public QuboProblem(TEncoding encoding) {
        this.encoding = encoding;
        this.quboPenalties = new ArrayList<>();
    }

    public QuboProblem<TEncoding, T> addAll(List<WeightedQuboPenalty<T>> quboPenalties) {
        this.quboPenalties.addAll(quboPenalties);
        return this;
    }

    public QuboProblem<TEncoding, T> add(double weight, IQuboPenalty<T> penalty) {
        return this.add(new WeightedQuboPenalty<>(weight, penalty));
    }

    public QuboProblem<TEncoding, T> add(WeightedQuboPenalty<T> penalty) {
        this.quboPenalties.add(penalty);
        return this;
    }

    @Override
    public QuboMatrix buildQuboMatrix() {
        final var variables = this.encoding.getVariables();
        final var quboMatrix = new QuboMatrix(variables.size());
        final var quboMatrixManipulation = quboMatrix.manipulate();

        // Apply diagonal/linear coefficients from penalties
        quboMatrixManipulation.addDiagonal(matrixIndex -> {
            final var index = matrixIndex.row(); // row == col
            final var variable = variables.get(index);
            return this.quboPenalties.stream()
                    .mapToDouble(penalty -> penalty.getLinearCoefficient(index, variable))
                    .sum();
        });

        // Apply non-diagonal/quadratic coefficients from penalties
        quboMatrixManipulation.addNonDiagonal(matrixIndex -> {
            if (matrixIndex.col() < matrixIndex.row()) {
                throw new IllegalArgumentException();
            }

            final var variablePair = this.encoding.getVariablePair(matrixIndex);
            return this.quboPenalties.stream()
                    .mapToDouble(penalty -> penalty.getQuadraticCoefficient(matrixIndex, variablePair))
                    .sum();
        });

        return new QuboMatrix(quboMatrixManipulation.toMatrix());
    }

    @Override
    public double getOffsetValue() {
        return this.quboPenalties.stream()
                .map(WeightedQuboPenalty::getOffsetValue)
                .reduce(0.0, Double::sum);
    }
}
