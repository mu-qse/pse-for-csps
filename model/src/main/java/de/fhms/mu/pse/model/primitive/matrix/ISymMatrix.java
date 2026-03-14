package de.fhms.mu.pse.model.primitive.matrix;

import java.util.List;

public interface ISymMatrix<T> extends IMatrix<T> {
    int length();
    List<MatrixIndex> getDiagonalIndices();
    List<MatrixIndex> getNonDiagonalIndices();
    List<MatrixValue<T>> getDiagonalValues();
    List<MatrixValue<T>> getNonDiagonalValues();
}
