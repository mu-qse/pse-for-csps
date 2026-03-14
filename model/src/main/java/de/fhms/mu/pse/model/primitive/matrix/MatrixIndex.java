package de.fhms.mu.pse.model.primitive.matrix;

public record MatrixIndex(int row, int col) {
    public boolean isDiagonal() {
        return this.row == this.col;
    }

    public MatrixIndex swap() {
        return new MatrixIndex(this.col, this.row);
    }

    @Override
    public String toString() {
        return String.format("(%d, %d)", this.row, this.col);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MatrixIndex(int otherRow, int otherCol))) {
            return false;
        }
        return this.row == otherRow && this.col == otherCol;
    }

    @Override
    public int hashCode() {
        return 31 * this.row + this.col;
    }
}
