package de.fhms.mu.pse.model.primitive.matrix.manipulation;

import de.fhms.mu.pse.model.primitive.matrix.ISymMatrix;
import de.fhms.mu.pse.model.primitive.matrix.MatrixIndex;

import java.util.function.Function;

public abstract class SymManipulationContext<T, TMatrix extends ISymMatrix<T>>
        extends ManipulationContext<T, TMatrix>
        implements ISymManipulationContext<T, TMatrix> {
    public SymManipulationContext(TMatrix matrix) {
        super(matrix);
    }

    @Override
    public SymManipulationContext<T, TMatrix> fill(ISymMatrix<T> matrix) {
        this.fill(index -> matrix.get(index).getValue());
        return this;
    }

    @Override
    public SymManipulationContext<T, TMatrix> fillDiagonal(Function<MatrixIndex, T> supplier) {
        this.matrix.getDiagonalIndices().forEach(index -> this.set(index, supplier.apply(index)));
        return this;
    }

    @Override
    public SymManipulationContext<T, TMatrix> fillNonDiagonal(Function<MatrixIndex, T> supplier) {
        this.matrix.getNonDiagonalIndices().forEach(index -> this.set(index, supplier.apply(index)));
        return this;
    }
}
