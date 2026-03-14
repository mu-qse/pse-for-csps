package de.fhms.mu.pse.examples.sudoku;

import de.fhms.mu.pse.annotations.domain.AggregateRoot;
import de.fhms.mu.pse.annotations.domain.OneToMany;
import de.fhms.mu.pse.annotations.domain.Update;
import de.fhms.mu.pse.annotations.problem.SelectableElements;
import de.fhms.mu.pse.annotations.problem.constraint.LogicConstraint;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@AggregateRoot
@Getter
public class Sudoku {
    private final int blockLength;
    private final Cell[][] cells;
    private final int[] availableNumbers;

    public Sudoku(int blockLength) {
        this.blockLength = blockLength;
        this.cells = generateCells(blockLength);
        this.availableNumbers = generateAvailableNumbers(blockLength);
    }

    @OneToMany
    @SelectableElements
    public List<Cell> getCellsAsList() {
        return Arrays.stream(this.cells)
                .flatMap(Arrays::stream)
                .toList();
    }

    @SelectableElements
    public List<Integer> getAvailableNumbers() {
        return Arrays.stream(this.availableNumbers)
                .boxed()
                .toList();
    }

    @Update
    public Sudoku set(Cell cell) {
        return this.set(cell.getRow(), cell.getCol(), cell.getValue());
    }

    public Sudoku set(int row, int col, int value) {
        final var cell = new Cell(row, col, value);
        validatePlacement(cell);
        this.cells[row][col] = cell;
        return this;
    }

    @LogicConstraint(weight = 2)
    public boolean isPrefilledWithValue(Cell cell) {
        final var existingCell = this.cells[cell.getRow()][cell.getCol()];
        if (existingCell.isEmpty()) {
            return true;
        }

        return existingCell.isSameValue(cell);
    }

    @LogicConstraint
    public boolean isNotSameCell(Cell a, Cell b) {
        return !a.equals(b);
    }

    @LogicConstraint
    public boolean isNotSameValueInSameRow(Cell a, Cell b) {
        return !a.isSameValue(b) || !a.isSameRow(b);
    }

    @LogicConstraint
    public boolean isNotSameValueInSameCol(Cell a, Cell b) {
        return !a.isSameValue(b) || !a.isSameCol(b);
    }

    @LogicConstraint
    public boolean isNotSameValueInSameBlock(Cell a, Cell b) {
        return !a.isSameValue(b) || !this.isSameBlock(a, b);
    }

    public boolean isSameBlock(Cell a, Cell b) {
        final var aBlockRow = (int) Math.floor((double) a.getRow() / this.blockLength);
        final var aBlockCol = (int) Math.floor((double) a.getCol() / this.blockLength);
        final var bBlockRow = (int) Math.floor((double) b.getRow() / this.blockLength);
        final var bBlockCol = (int) Math.floor((double) b.getCol() / this.blockLength);

        return aBlockRow == bBlockRow && aBlockCol == bBlockCol;
    }

    @Override
    public String toString() {
        final var stats = Arrays.stream(this.cells)
                .flatMap(Arrays::stream)
                .map(cell -> Cell.valueToString(cell.getValue()))
                .map(String::length)
                .collect(Collectors.summarizingInt(Integer::intValue));
        final var pad = stats.getMax();
        return Arrays.stream(this.cells)
                .map(row -> "[ " + Arrays.stream(row)
                        .map(cell -> String.format("%" + pad + "s", Cell.valueToString(cell.getValue())))
                        .collect(Collectors.joining(", ")) + " ]"
                ).collect(Collectors.joining(",\n"));
    }

    private void validatePlacement(Cell cell) {
        final var existingCells = this.getCellsAsList();
        for (final var existingCell : existingCells) {
            if (!this.isNotSameCell(existingCell, cell)) {
                continue;
            }

            if (!this.isNotSameValueInSameCol(existingCell, cell)
                    || !this.isNotSameValueInSameRow(existingCell, cell)
                    || !this.isNotSameValueInSameBlock(existingCell, cell)) {
                throw new IllegalStateException("Cell conflicts with: " + cell);
            }
        }
    }

    private static Cell[][] generateCells(int blockLength) {
        if (blockLength < 2) {
            throw new IllegalArgumentException("Length must be at least 2");
        }

        final var gridLength = blockLength * blockLength;
        final var cells = new Cell[gridLength][gridLength];
        for (int row = 0; row < gridLength; row++) {
            for (int col = 0; col < gridLength; col++) {
                cells[row][col] = new Cell(row, col);
            }
        }
        return cells;
    }

    private static int[] generateAvailableNumbers(int blockLength) {
        return IntStream.rangeClosed(1, blockLength * blockLength).toArray();
    }
}
