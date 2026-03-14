package de.fhms.mu.pse.model.domain;

import lombok.Data;

import java.util.*;
import java.util.stream.Collectors;

@Data
public class ElementCombinationTuple<E> implements IDomainType<E> {
    private final Map<IDomainType<?>, Object> values = new HashMap<>();

    private final IDomainType<E> domainType;
    private final List<IManyToOneRelation<E, Object>> relations;

    public ElementCombinationTuple(ElementCombinationTuple<E> other) {
        this.domainType = other.getDomainType();
        this.relations = other.getRelations();
        this.fillValues(other);
    }

    public ElementCombinationTuple(IDomainType<E> domainType, List<IManyToOneRelation<E, Object>> relations) {
        this.domainType = domainType;
        this.relations = relations;
    }

    @Override
    public String getName() {
        return this.domainType.getName();
    }

    @Override
    public Class<E> getType() {
        return this.domainType.getType();
    }

    public void fillValues(ElementCombinationTuple<E> other) {
        this.values.putAll(other.getValues());
    }

    public <T, V> void setValue(IDomainType<T> type, V value) {
        this.values.put(type, value);
    }

    public <V> Optional<V> getValue(IDomainType<V> type) {
        if (!this.values.containsKey(type)) {
            return Optional.empty();
        }

        return Optional.of((V) this.values.get(type));
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ElementCombinationTuple<?> that)) {
            return false;
        }
        return Objects.equals(this.domainType, that.domainType);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.domainType);
    }

    @Override
    public String toString() {
        if (this.values.isEmpty()) {
            return this.domainType.getName();
        }

        return String.format("{%s}", this.values.values().stream()
                .map(Object::toString)
                .collect(Collectors.joining(", ")));
    }
}
