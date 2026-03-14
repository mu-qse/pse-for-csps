package de.fhms.mu.pse.examples.sudoku;

import de.fhms.mu.pse.model.domain.AbstractAggregateFactory;

public class SudokuFactory extends AbstractAggregateFactory<Sudoku> {
    private final int n;

    public SudokuFactory(int n) {
        super(Sudoku.class);

        this.n = n;
    }

    @Override
    public Sudoku create(Object... args) {
        return new Sudoku(this.n)
                .set(0, 2, 1)
                .set(1, 1, 3)
                .set(2, 2, 2)
                .set(3, 1, 1);
    }
}
