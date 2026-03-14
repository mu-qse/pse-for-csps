package de.fhms.mu.pse.model.primitive.matrix.manipulation;

import de.fhms.mu.pse.model.primitive.matrix.ISymMatrix;
import de.fhms.mu.pse.model.primitive.matrix.MatrixIndex;

import java.util.function.Function;

public interface ISymManipulationContext<T, TMatrix extends ISymMatrix<T>> extends IManipulationContext<T, TMatrix> {
    ISymManipulationContext<T, TMatrix> fill(ISymMatrix<T> matrix);
    ISymManipulationContext<T, TMatrix> fillDiagonal(Function<MatrixIndex, T> supplier);
    ISymManipulationContext<T, TMatrix> fillNonDiagonal(Function<MatrixIndex, T> supplier);
    ISymManipulationContext<T, TMatrix> add(ISymMatrix<T> other);
    ISymManipulationContext<T, TMatrix> addDiagonal(Function<MatrixIndex, T> supplier);
    ISymManipulationContext<T, TMatrix> addNonDiagonal(Function<MatrixIndex, T> supplier);
    ISymManipulationContext<T, TMatrix> pad(int length);
}
