package de.fhms.mu.pse.model.problem;

import de.fhms.mu.pse.model.domain.IDomainType;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Data
@RequiredArgsConstructor
public class CombinatorialProblemElementSet<T> {
    private final String name;
    private final IDomainType<T> reference;
    private final List<T> elements;

    public CombinatorialProblemElementSet(IDomainType<T> reference, List<T> elements) {
        this(reference.getName(), reference, elements);
    }

    public Class<List<T>> getType() {
        return (Class<List<T>>) this.elements.getClass();
    }

    public List<T> get() {
        return this.elements;
    }

    public void set(List<T> instances) {
        this.elements.clear();
        this.elements.addAll(instances);
    }

    public int count() {
        return this.elements.size();
    }

    @Override
    public String toString() {
        return String.format("%s: [%s]", this.getName(), this.elements.stream()
                .map(Objects::toString)
                .collect(Collectors.joining(", ")));
    }
}
