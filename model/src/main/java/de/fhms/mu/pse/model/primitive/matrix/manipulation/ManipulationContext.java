package de.fhms.mu.pse.model.primitive.matrix.manipulation;

import de.fhms.mu.pse.model.primitive.matrix.IMatrix;
import de.fhms.mu.pse.model.primitive.matrix.MatrixIndex;
import lombok.RequiredArgsConstructor;

import java.util.function.Function;

@RequiredArgsConstructor
public abstract class ManipulationContext<T, TMatrix extends IMatrix<T>>
        implements IManipulationContext<T, TMatrix> {
    protected final TMatrix matrix;

    @Override
    public ManipulationContext<T, TMatrix> set(MatrixIndex index, T value) {
        this.matrix.set(index, value);
        return this;
    }

    @Override
    public ManipulationContext<T, TMatrix> reset() {
        return fill(index -> null);
    }

    @Override
    public ManipulationContext<T, TMatrix> fill(Function<MatrixIndex, T> supplier) {
        this.matrix.getIndices().forEach(index -> this.matrix.set(index, supplier.apply(index)));
        return this;
    }

    @Override
    public TMatrix toMatrix() {
        return this.matrix;
    }
}
