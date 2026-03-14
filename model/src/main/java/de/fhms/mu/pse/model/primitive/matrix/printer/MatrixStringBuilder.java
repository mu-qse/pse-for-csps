package de.fhms.mu.pse.model.primitive.matrix.printer;

import de.fhms.mu.pse.model.primitive.matrix.IMatrix;
import de.fhms.mu.pse.model.primitive.matrix.MatrixIndex;
import de.fhms.mu.pse.model.primitive.matrix.MatrixValue;

import java.util.List;
import java.util.stream.Collectors;

public class MatrixStringBuilder<M extends IMatrix<T>, T> implements IMatrixStringBuilder<M, T> {
    @Override
    public String buildString(M matrix) {
        final var pad = this.getValueMaxLength(matrix);

        final var rowIndices = matrix.getRowIndices();
        return rowIndices.stream()
                .map(colIndices -> this.buildRowString(matrix, colIndices, pad))
                .collect(Collectors.joining(",\n"));
    }

    protected String buildRowString(M matrix, List<MatrixIndex> indices, int pad) {
        return "[ " + indices.stream()
                .map(matrix::get)
                .map(MatrixValue::getValue)
                .map(value -> String.format("%" + pad + "s", matrix.format(value)))
                .collect(Collectors.joining(", ")) + " ]";
    }

    private int getValueMaxLength(IMatrix<T> matrix) {
        final var values = matrix.getValues().stream()
                .map(MatrixValue::getValue);
        final var stats = values
                .map(matrix::format)
                .map(String::length)
                .collect(Collectors.summarizingInt(Integer::intValue));
        return stats.getMax();
    }
}
