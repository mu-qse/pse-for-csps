package de.fhms.mu.pse.examples.queens;

import de.fhms.mu.pse.annotations.domain.AggregateRoot;
import de.fhms.mu.pse.annotations.domain.OneToMany;
import de.fhms.mu.pse.annotations.domain.Update;
import de.fhms.mu.pse.annotations.problem.SelectableElements;
import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@AggregateRoot
@Data
public class Chessboard {
    private final int length;
    private final Row[] rows;
    private final Queen[] availableQueens;

    private final List<Queen> placedQueens = new ArrayList<>();

    public Chessboard(int length) {
        this.length = length;
        this.rows = generateRows(length);
        this.availableQueens = generateAvailableQueens(this.rows);
    }

    @OneToMany
    @SelectableElements
    public List<Row> getRowsAsList() {
        return Arrays.stream(this.rows)
                .toList();
    }

    @SelectableElements
    public List<Queen> getAvailableQueens() {
        return Arrays.stream(this.availableQueens)
                .toList();
    }

    @OneToMany
    public List<Queen> getPlacedQueens() {
        return Collections.unmodifiableList(this.placedQueens);
    }

    @Update
    public Chessboard place(Queen queen) {
        validatePlacement(queen);
        this.placedQueens.add(queen);
        return this;
    }

    public boolean isFilled(Cell cell) {
        return this.isFilled(cell.row(),  cell.col());
    }

    public boolean isFilled(int rowIndex, int colIndex) {
        return this.placedQueens.stream()
                .anyMatch(queen -> queen.isInCell(rowIndex, colIndex));
    }

    @Override
    public String toString() {
        final var cells = new Cell[this.length][this.length];
        for (int row = 0; row < this.length; row++) {
            for (int col = 0; col < this.length; col++) {
                cells[row][col] = new Cell(row, col);
            }
        }

        return Arrays.stream(cells)
                .map(row -> "[ " + Arrays.stream(row)
                        .map(cell -> this.isFilled(cell) ? "Q" : "-")
                        .collect(Collectors.joining(", ")) + " ]"
                ).collect(Collectors.joining(",\n"));
    }

    private void validatePlacement(Queen queen) {
        if (this.placedQueens.size() == this.length) {
            throw new IllegalArgumentException("Number of placed queens: " + this.length);
        }

        final var rowsAsList = this.getRowsAsList();
        final var queenRow = queen.getRow();
        if (!rowsAsList.contains(queenRow)) {
            throw new IllegalStateException("Queen is not within rows: " + queenRow);
        }

        final var existingQueens = this.getPlacedQueens();
        for (var existingQueen : existingQueens) {
            if (!queen.isSafe(existingQueen)) {
                throw new IllegalStateException("Queen conflicts with: " + queen.getRow());
            }
        }
    }

    private static Row[] generateRows(int length) {
        if (length < 4) {
            throw new IllegalArgumentException("Length must be at least 4");
        }

        final var rows = new Row[length];
        for (int rowIndex = 0; rowIndex < length; rowIndex++) {
            rows[rowIndex] = new Row(rowIndex);
        }
        return rows;
    }

    private static Queen[] generateAvailableQueens(Row[] rows) {
        if (rows.length < 4) {
            throw new IllegalArgumentException("Length must be at least 4");
        }

        final var queens = new Queen[rows.length];
        for (int rowIndex = 0; rowIndex < rows.length; rowIndex++) {
            queens[rowIndex] = new Queen(rowIndex);
        }
        return queens;
    }
}
