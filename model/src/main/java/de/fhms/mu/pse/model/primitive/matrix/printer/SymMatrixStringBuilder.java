package de.fhms.mu.pse.model.primitive.matrix.printer;

import de.fhms.mu.pse.model.primitive.matrix.ISymMatrix;
import de.fhms.mu.pse.model.primitive.matrix.MatrixIndex;

import java.util.List;
import java.util.stream.Collectors;

public class SymMatrixStringBuilder<M extends ISymMatrix<T>, T> extends MatrixStringBuilder<M, T> {
    @Override
    protected String buildRowString(M matrix, List<MatrixIndex> indices, int pad) {
        return "[ " + indices.stream()
                .map(index -> {
                    if (index.col() < index.row()) {
                        return String.format("%" + pad + "s", "-");
                    }

                    final var value = matrix.get(index).getValue();
                    return String.format("%" + pad + "s", matrix.format(value));
                })
                .collect(Collectors.joining(", ")) + " ]";
    }
}
