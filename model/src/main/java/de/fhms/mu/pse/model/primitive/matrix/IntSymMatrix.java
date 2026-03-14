package de.fhms.mu.pse.model.primitive.matrix;

import de.fhms.mu.pse.model.primitive.Bitstring;
import de.fhms.mu.pse.model.primitive.matrix.manipulation.IntSymManipulationContext;

public class IntSymMatrix extends SymMatrix<Integer> {
    public IntSymMatrix(ISymMatrix<Integer> other) {
        this(other.length());
        this.manipulate().fill(other);
    }

    public IntSymMatrix(int length) {
        super(length);
        this.manipulate().reset();
    }

    public IntSymManipulationContext manipulate() {
        return new IntSymManipulationContext(this);
    }

    public Integer apply(Bitstring bitstring) {
        return this.getValues()
                .stream()
                .filter(MatrixValue::isPresent)
                .filter(indexAndValue -> {
                    final var index = indexAndValue.getIndex();
                    return bitstring.getAsBoolean(index.row()) && bitstring.getAsBoolean((index.col()));
                })
                .map(MatrixValue::getValue)
                .reduce(Integer::sum)
                .orElse(0);
    }

    @Override
    public String format(Integer value) {
        return value != null && value != 0 ? super.format(value) : "-";
    }
}
