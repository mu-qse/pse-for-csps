package de.fhms.mu.pse.model.primitive.matrix;

import de.fhms.mu.pse.model.primitive.matrix.printer.MatrixStringBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Implementation of a matrix.
 */
public class Matrix<T> implements IMatrix<T> {
    private final int rowsCount;
    private final int colsCount;
    private final List<MatrixIndex> indices;
    private final List<MatrixValue<T>> values;

    protected Matrix(int rowsCount, int colsCount) {
        this(rowsCount, colsCount, () -> generateIndices(rowsCount, colsCount));
    }

    protected Matrix(int rowsCount, int colsCount, Supplier<List<MatrixIndex>> indicesSupplier) {
        this.rowsCount = rowsCount;
        this.colsCount = colsCount;

        this.indices = indicesSupplier.get();
        this.values = generateValues(this.indices);
    }

    @Override
    public int rowsCount() {
        return this.rowsCount;
    }

    @Override
    public int colsCount() {
        return this.colsCount;
    }

    @Override
    public int entriesCount() {
        return this.indices.size();
    }

    @Override
    public MatrixValue<T> get(int row, int col) {
        final var index = getIndex(row, col);
        return this.get(index);
    }

    @Override
    public MatrixValue<T> get(MatrixIndex index) {
        return this.get(index.row(), index.col());
    }

    @Override
    public void set(int row, int col, T value) {
        final var index = getIndex(row, col);
        this.get(index).setValue(value);
    }

    @Override
    public void set(MatrixIndex index, T value) {
        this.set(index.row(), index.col(), value);
    }

    @Override
    public List<MatrixIndex> getIndices() {
        return this.indices;
    }

    @Override
    public List<List<MatrixIndex>> getRowIndices() {
        return IntStream.range(0, this.rowsCount)
                .mapToObj(row -> IntStream.range(0, this.colsCount)
                        .mapToObj(col -> new MatrixIndex(row, col))
                        .toList()).toList();
    }

    @Override
    public List<MatrixValue<T>> getValues() {
        return this.values;
    }

    @Override
    public List<MatrixValue<T>> getValues(List<MatrixIndex> indices) {
        return indices.stream()
                .map(this::get)
                .toList();
    }

    @Override
    public String toString() {
        return new MatrixStringBuilder<Matrix<T>, T>().buildString(this);
    }

    @Override
    public String format(MatrixValue<T> value) {
        return value != null ? this.format(value.getValue()) : "-";
    }

    @Override
    public String format(T value) {
        return value != null ? value.toString() : "-";
    }

    @Override
    public List<List<T>> toList() {
        return this.getRowIndices().stream()
                .map(rowIndices -> rowIndices.stream()
                        .map(this::get)
                        .map(MatrixValue::getValue)
                        .toList())
                .toList();
    }

    protected int getIndex(int row, int col) {
        return row * this.colsCount + col;
    }

    protected MatrixValue<T> get(int index) {
        return this.values.get(index);
    }

    protected void set(int index, T value) {
        this.get(index).setValue(value);
    }

    private static List<MatrixIndex> generateIndices(int rowsCount, int colsCount) {
        final var size = rowsCount * colsCount;
        final var indices = new ArrayList<MatrixIndex>(size);
        for (int row = 0; row < rowsCount; row++) {
            for (int col = 0; col < colsCount; col++) {
                indices.add(new MatrixIndex(row, col));
            }
        }
        indices.trimToSize();
        return indices;
    }

    /*private static <T> MatrixValue<T>[][] generateValuesMap(int rowsCount, int colsCount, List<MatrixIndex> indices) {
        final var map = (MatrixValue<T>[][]) Array.newInstance(MatrixValue.class, rowsCount, colsCount);
        indices.forEach(index -> map[index.row()][index.col()] = new MatrixValue<>(index));
        return map;
    }*/

    private static <T> List<MatrixValue<T>> generateValues(List<MatrixIndex> indices) {
        return indices.stream()
                .map(index -> new MatrixValue<T>(index))
                .collect(Collectors.toList());
    }
}
