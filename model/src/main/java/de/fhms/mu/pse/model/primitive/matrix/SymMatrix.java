package de.fhms.mu.pse.model.primitive.matrix;

import de.fhms.mu.pse.model.primitive.matrix.printer.SymMatrixStringBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of a symmetric matrix.
 * O( (n * (n - 1)) / 2 + n )
 */
public abstract class SymMatrix<T> extends Matrix<T> implements ISymMatrix<T> {
    private final List<MatrixIndex> diagonalIndices;
    private final List<MatrixIndex> nonDiagonalIndices;

    protected SymMatrix(int length) {
        super(length, length, () -> generateIndices(length));

        this.diagonalIndices = generateDiagonalIndices(length);
        this.nonDiagonalIndices = generateNonDiagonalIndices(length);
    }

    @Override
    public int length() {
        return this.rowsCount();
    }

    @Override
    public List<MatrixIndex> getDiagonalIndices() {
        return this.diagonalIndices;
    }

    @Override
    public List<MatrixValue<T>> getDiagonalValues() {
        return this.getValues(this.diagonalIndices);
    }

    @Override
    public List<MatrixIndex> getNonDiagonalIndices() {
        return this.nonDiagonalIndices;
    }

    @Override
    public List<MatrixValue<T>> getNonDiagonalValues() {
        return this.getValues(this.nonDiagonalIndices);
    }

    @Override
    public String format(MatrixValue<T> value) {
        return value.getIndex().col() <= value.getIndex().row() ? this.format(value.getValue()) : "-";
    }

    @Override
    public String toString() {
        return new SymMatrixStringBuilder<SymMatrix<T>, T>().buildString(this);
    }

    @Override
    protected int getIndex(int row, int col) {
        if (col < row) {
            return this.getIndex(col, row);
        }

        // return col + (row * ((this.length() - row + this.length() - 1) / 2));
        return (int) (row * this.length() + col - (row * ((1 + row) / 2.0)));
    }

    private static List<MatrixIndex> generateIndices(int length) {
        final var size = getNumberOfEntries(length);
        final var indices = new ArrayList<MatrixIndex>(size);
        for (int row = 0; row < length; row++) {
            for (int col = row; col < length; col++) {
                indices.add(new MatrixIndex(row, col));
            }
        }
        indices.trimToSize();
        return indices;
    }

    private static ArrayList<MatrixIndex> generateDiagonalIndices(int length) {
        final var size = getNumberOfDiagonalEntries(length);
        final var indices = new ArrayList<MatrixIndex>(size);
        for (int row = 0, col = 0; row < length && col < length; row++, col++) {
            indices.add(new MatrixIndex(row, col));
        }
        indices.trimToSize();
        return indices;
    }

    private static ArrayList<MatrixIndex> generateNonDiagonalIndices(int length) {
        final var size = getNumberOfNonDiagonalEntries(length);
        final var indices = new ArrayList<MatrixIndex>(size);
        for (int row = 0; row < length; row++) {
            for (int col = row + 1; col < length; col++) {
                indices.add(new MatrixIndex(row, col));
            }
        }
        indices.trimToSize();
        return indices;
    }

    private static int getNumberOfEntries(int length) {
        return getNumberOfDiagonalEntries(length) + getNumberOfNonDiagonalEntries(length);
    }

    private static int getNumberOfDiagonalEntries(int length) {
        return length;
    }

    private static int getNumberOfNonDiagonalEntries(int length) {
        return (length * (length - 1)) / 2;
    }
}
