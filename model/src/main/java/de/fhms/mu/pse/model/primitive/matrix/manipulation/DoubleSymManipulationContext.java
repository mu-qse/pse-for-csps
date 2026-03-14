package de.fhms.mu.pse.model.primitive.matrix.manipulation;

import de.fhms.mu.pse.model.primitive.matrix.DoubleSymMatrix;
import de.fhms.mu.pse.model.primitive.matrix.ISymMatrix;
import de.fhms.mu.pse.model.primitive.matrix.MatrixIndex;

import java.util.function.Function;

public class DoubleSymManipulationContext extends SymManipulationContext<Double, DoubleSymMatrix> {
    public DoubleSymManipulationContext(DoubleSymMatrix matrix) {
        super(matrix);
    }

    @Override
    public DoubleSymManipulationContext set(MatrixIndex index, Double value) {
        super.set(index, value);
        return this;
    }

    @Override
    public DoubleSymManipulationContext reset() {
        return this.fill(index -> 0.0);
    }

    @Override
    public DoubleSymManipulationContext fill(ISymMatrix<Double> other) {
        super.fill(other);
        return this;
    }

    @Override
    public DoubleSymManipulationContext fill(Function<MatrixIndex, Double> supplier) {
        super.fill(supplier);
        return this;
    }

    @Override
    public DoubleSymManipulationContext fillDiagonal(Function<MatrixIndex, Double> supplier) {
        super.fillDiagonal(supplier);
        return this;
    }

    @Override
    public DoubleSymManipulationContext fillNonDiagonal(Function<MatrixIndex, Double> supplier) {
        super.fillNonDiagonal(supplier);
        return this;
    }

    @Override
    public ISymManipulationContext<Double, DoubleSymMatrix> add(ISymMatrix<Double> other) {
        if (this.matrix.length() != other.length()) {
            throw new IllegalArgumentException(String.format("Matrix length mismatch: %d != %d", this.matrix.length(), other.length()));
        }

        this.fill(index -> this.matrix.get(index).getValue() + other.get(index).getValue());
        return this;
    }

    @Override
    public DoubleSymManipulationContext add(Function<MatrixIndex, Double> supplier) {
        this.matrix.getIndices().forEach(index -> this.set(index, this.matrix.get(index).getValue() + supplier.apply(index)));
        return this;
    }

    @Override
    public DoubleSymManipulationContext addDiagonal(Function<MatrixIndex, Double> supplier) {
        this.matrix.getDiagonalIndices().forEach(index -> this.set(index, this.matrix.get(index).getValue() + supplier.apply(index)));
        return this;
    }

    @Override
    public DoubleSymManipulationContext addNonDiagonal(Function<MatrixIndex, Double> supplier) {
        this.matrix.getNonDiagonalIndices().forEach(index -> this.set(index, this.matrix.get(index).getValue() + supplier.apply(index)));
        return this;
    }

    @Override
    public DoubleSymManipulationContext scale(Double scalar) {
        this.fill(index -> this.matrix.get(index).getValue() * scalar);
        return this;
    }

    @Override
    public DoubleSymManipulationContext pad(int length) {
        if (length < this.matrix.length()) {
            throw new IllegalArgumentException(String.format("Matrix length mismatch: %d < %d", length, this.matrix.length()));
        }

        final var paddedMatrix = new DoubleSymMatrix(length);
        return paddedMatrix.manipulate().fill(index -> {
            if (index.row() >= this.matrix.length() || index.col() >= this.matrix.length()) {
                return 0.0;
            }

            return this.matrix.get(index).getValue();
        });
    }
}
