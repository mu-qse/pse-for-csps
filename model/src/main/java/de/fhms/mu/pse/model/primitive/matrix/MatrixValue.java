package de.fhms.mu.pse.model.primitive.matrix;

import lombok.Data;

import java.util.Objects;

@Data
public class MatrixValue<T> {
    private final MatrixIndex index;
    private T value;

    public boolean isEmpty() {
        return this.value == null;
    }

    public boolean isPresent() {
        return this.value != null;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MatrixValue<?> that)) {
            return false;
        }
        return Objects.equals(this.index, that.index);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.index);
    }
}
