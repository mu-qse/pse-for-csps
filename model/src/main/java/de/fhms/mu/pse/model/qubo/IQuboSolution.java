package de.fhms.mu.pse.model.qubo;

import de.fhms.mu.pse.model.primitive.Bitstring;

import java.util.stream.Stream;

public interface IQuboSolution<T> {
    Bitstring getBitstring();
    Stream<T> getElements();
}
