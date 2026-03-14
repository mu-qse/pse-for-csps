package de.fhms.mu.pse.model.primitive.matrix;

import de.fhms.mu.pse.model.primitive.Bitstring;
import de.fhms.mu.pse.model.primitive.matrix.manipulation.DoubleSymManipulationContext;

import java.util.Locale;

public class DoubleSymMatrix extends SymMatrix<Double> {
    public DoubleSymMatrix(ISymMatrix<Double> other) {
        this(other.length());
        this.manipulate().fill(other);
    }

    public DoubleSymMatrix(int length) {
        super(length);
        this.manipulate().reset();
    }

    public DoubleSymManipulationContext manipulate() {
        return new DoubleSymManipulationContext(this);
    }

    public Double apply(Bitstring bitstring) {
        return this.getValues()
                .stream()
                .filter(MatrixValue::isPresent)
                .filter(indexAndValue -> {
                    final var index = indexAndValue.getIndex();
                    if (index.col() < index.row()) {
                        throw new IllegalArgumentException();
                    }

                    return bitstring.getAsBoolean(index.row()) && bitstring.getAsBoolean((index.col()));
                })
                .map(MatrixValue::getValue)
                .reduce(Double::sum)
                .orElse(0.0);
    }

    @Override
    public String format(Double value) {
        return value != null && value != 0 ? String.format(Locale.ENGLISH, "%.2f", value) : "-";
    }
}
