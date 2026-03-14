package de.fhms.mu.pse.model.primitive.matrix.manipulation;

import de.fhms.mu.pse.model.primitive.matrix.ISymMatrix;
import de.fhms.mu.pse.model.primitive.matrix.IntSymMatrix;
import de.fhms.mu.pse.model.primitive.matrix.MatrixIndex;

import java.util.function.Function;

public class IntSymManipulationContext extends SymManipulationContext<Integer, IntSymMatrix> {
    public IntSymManipulationContext(IntSymMatrix matrix) {
        super(matrix);
    }

    @Override
    public IntSymManipulationContext set(MatrixIndex index, Integer value) {
        super.set(index, value);
        return this;
    }

    @Override
    public IntSymManipulationContext reset() {
        return this.fill(index -> 0);
    }

    @Override
    public IntSymManipulationContext fill(ISymMatrix<Integer> matrix) {
        super.fill(matrix);
        return this;
    }

    @Override
    public IntSymManipulationContext fill(Function<MatrixIndex, Integer> supplier) {
        super.fill(supplier);
        return this;
    }

    @Override
    public IntSymManipulationContext fillDiagonal(Function<MatrixIndex, Integer> supplier) {
        super.fillDiagonal(supplier);
        return this;
    }

    @Override
    public IntSymManipulationContext fillNonDiagonal(Function<MatrixIndex, Integer> supplier) {
        super.fillNonDiagonal(supplier);
        return this;
    }

    @Override
    public ISymManipulationContext<Integer, IntSymMatrix> add(ISymMatrix<Integer> other) {
        if (this.matrix.length() != other.length()) {
            throw new IllegalArgumentException(String.format("Matrix length mismatch: %d != %d", this.matrix.length(), other.length()));
        }

        this.fill(index -> this.matrix.get(index).getValue() + other.get(index).getValue());
        return this;
    }

    @Override
    public IntSymManipulationContext add(Function<MatrixIndex, Integer> supplier) {
        this.matrix.getIndices().forEach(index -> this.set(index, this.matrix.get(index).getValue() + supplier.apply(index)));
        return this;
    }

    @Override
    public IntSymManipulationContext addDiagonal(Function<MatrixIndex, Integer> supplier) {
        this.matrix.getDiagonalIndices().forEach(index -> this.set(index, this.matrix.get(index).getValue() + supplier.apply(index)));
        return this;
    }

    @Override
    public IntSymManipulationContext addNonDiagonal(Function<MatrixIndex, Integer> supplier) {
        this.matrix.getNonDiagonalIndices().forEach(index -> this.set(index, this.matrix.get(index).getValue() + supplier.apply(index)));
        return this;
    }

    @Override
    public IntSymManipulationContext scale(Integer scalar) {
        this.fill(index -> this.matrix.get(index).getValue() * scalar);
        return this;
    }

    @Override
    public IntSymManipulationContext pad(int length) {
        if (length < this.matrix.length()) {
            throw new IllegalArgumentException(String.format("Matrix length mismatch: %d < %d", length, this.matrix.length()));
        }

        final var paddedMatrix = new IntSymMatrix(length);
        return paddedMatrix.manipulate().fill(index -> {
            if (index.row() >= this.matrix.length() || index.col() >= this.matrix.length()) {
                return 0;
            }

            return this.matrix.get(index).getValue();
        });
    }
}
