package de.fhms.mu.pse.model.qubo;

import de.fhms.mu.pse.model.primitive.Bitstring;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class QuboSolution<
        T
> implements IQuboSolution<
        T
> {
    private final Bitstring bitstring;
    private final List<T> elements;

    @Override
    public Stream<T> getElements() {
        return this.elements.stream();
    }

    @Override
    public Bitstring getBitstring() {
        return this.bitstring;
    }

    @Override
    public String toString() {
        return String.format("[%s]", this.elements.stream()
                .map(Object::toString)
                .collect(Collectors.joining(", ")));
    }
}
