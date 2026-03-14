package de.fhms.mu.pse.model.qubo;

import de.fhms.mu.pse.model.primitive.matrix.DoubleSymMatrix;
import de.fhms.mu.pse.model.primitive.matrix.ISymMatrix;
import de.fhms.mu.pse.model.primitive.matrix.MatrixIndex;

import java.util.function.Function;

/**
 * Representation of a qubo matrix.
 * Always squared and symmetric.
 */
public class QuboMatrix extends DoubleSymMatrix {
    public QuboMatrix(ISymMatrix<Double> matrix) {
        super(matrix);
    }

    public QuboMatrix(int length) {
        super(length);
    }

    public static QuboMatrix empty() {
        return new QuboMatrix(0);
    }

    public static QuboMatrix create(int length, Function<MatrixIndex, Double> valueSupplier) {
        final var matrix = new DoubleSymMatrix(length);
        final var manipulationContext = matrix.manipulate().fill(valueSupplier);
        return new QuboMatrix(manipulationContext.toMatrix());
    }
}
