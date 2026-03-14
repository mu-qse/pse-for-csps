package de.fhms.mu.pse.model.primitive.matrix;

import java.util.List;

public interface IMatrix<T> {
    int rowsCount();
    int colsCount();
    int entriesCount();
    void set(int row, int col, T value);
    void set(MatrixIndex index, T value);
    MatrixValue<T> get(int row, int col);
    MatrixValue<T> get(MatrixIndex index);
    List<MatrixIndex> getIndices();
    List<List<MatrixIndex>> getRowIndices();
    List<MatrixValue<T>> getValues();
    List<MatrixValue<T>> getValues(List<MatrixIndex> indices);
    String format(MatrixValue<T> value);
    String format(T value);
    List<List<T>> toList();
}
