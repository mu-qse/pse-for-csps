package de.fhms.mu.pse.examples.queens;

import de.fhms.mu.pse.model.domain.AbstractAggregateFactory;

public class ChessboardFactory extends AbstractAggregateFactory<Chessboard> {
    private final int n;

    public ChessboardFactory(int n) {
        super(Chessboard.class);

        this.n = n;
    }

    @Override
    public Chessboard create(Object... args) {
        return new Chessboard(this.n);
    }
}
