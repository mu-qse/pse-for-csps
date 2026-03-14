package de.fhms.mu.pse.examples.queens;

import de.fhms.mu.pse.annotations.domain.ValueObject;

import java.util.Objects;

@ValueObject
@Deprecated
public record Cell(int row, int col) {
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Cell(int otherRow, int otherCol))) {
            return false;
        }
        return this.row == otherRow && this.col == otherCol;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.row, this.col);
    }

    @Override
    public String toString() {
        return String.format("(%d, %d)", this.row, this.col);
    }

    public static boolean isSameRow(Cell a, Cell b) {
        return a.row == b.row;
    }

    public static boolean isSameCol(Cell a, Cell b) {
        return a.col == b.col;
    }

    public static boolean isSameDiagonal(Cell a, Cell b) {
        return Math.abs(a.row() - b.row()) == Math.abs(a.col() - b.col());
    }
}
