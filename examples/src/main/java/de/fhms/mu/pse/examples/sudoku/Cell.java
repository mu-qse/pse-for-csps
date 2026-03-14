package de.fhms.mu.pse.examples.sudoku;

import de.fhms.mu.pse.annotations.domain.Create;
import de.fhms.mu.pse.annotations.domain.ManyToOne;
import de.fhms.mu.pse.annotations.domain.Update;
import de.fhms.mu.pse.annotations.domain.ValueObject;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Objects;

@ValueObject
@Data
@AllArgsConstructor
public class Cell {
    private final int row;
    private final int col;
    private final Integer value;

    @Create
    public Cell(int row, int col) {
        this(row, col, 0);
    }

    public Cell(Cell other, Integer value) {
        this(other.row, other.col, value);
    }

    @ManyToOne
    public Integer getValue() {
        return this.value;
    }

    @Update
    public Cell withValue(Integer value) {
        return new Cell(this, value);
    }

    public boolean isFilled() {
        return !this.isEmpty();
    }

    public boolean isEmpty() {
        return this.value == null || this.value == 0;
    }

    public boolean isSameCol(Cell other) {
        return this.col == other.col;
    }

    public boolean isSameRow(Cell other) {
        return this.row == other.row;
    }

    public boolean isSameValue(Cell other) {
        return Objects.equals(this.value, other.value);
    }

    @Override
    public String toString() {
        return this.isFilled()
                ? String.format("(%d, %d): %d", this.row, this.col, this.value)
                : String.format("(%d, %d)", this.row, this.col);
    }

    @Override
    public boolean equals(Object other) {
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        Cell otherCell = (Cell) other;
        return this.row == otherCell.row && this.col == otherCell.col;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.row, this.col);
    }

    public static String valueToString(Integer value) {
        return value != null && value != 0 ? value.toString() : "-";
    }

}
