package de.fhms.mu.pse.model.primitive.matrix.manipulation;

import de.fhms.mu.pse.model.primitive.matrix.IMatrix;
import de.fhms.mu.pse.model.primitive.matrix.MatrixIndex;

import java.util.function.Function;

public interface IManipulationContext<T, TMatrix extends IMatrix<T>> {
    IManipulationContext<T, TMatrix> set(MatrixIndex index, T value);
    IManipulationContext<T, TMatrix> reset();
    IManipulationContext<T, TMatrix> fill(Function<MatrixIndex, T> supplier);
    IManipulationContext<T, TMatrix> add(Function<MatrixIndex, T> supplier);
    IManipulationContext<T, TMatrix> scale(T scalar);
    TMatrix toMatrix();
}
