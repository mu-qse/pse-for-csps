package de.fhms.mu.pse.model.primitive.matrix.printer;

import de.fhms.mu.pse.model.primitive.matrix.IMatrix;

public interface IMatrixStringBuilder<M extends IMatrix<T>, T> {
    String buildString(M matrix);
}
