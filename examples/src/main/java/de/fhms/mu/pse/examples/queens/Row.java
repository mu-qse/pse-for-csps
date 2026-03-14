package de.fhms.mu.pse.examples.queens;

import de.fhms.mu.pse.annotations.domain.ValueObject;

@ValueObject
public record Row(int index) {
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Row row)) {
            return false;
        }

        return this.index == row.index;
    }

    @Override
    public String toString() {
        return String.format("%d", this.index);
    }
}
