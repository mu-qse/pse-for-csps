package de.fhms.mu.pse.model.domain;

import lombok.Data;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

@Data
public class DefaultOneToManyRelation<A, B> implements IOneToManyRelation<A, B> {
    private final IDomainType<A> a;
    private final IDomainType<B> b;
    private final Function<A, List<B>> accessor;
    private final BiFunction<A, B, A> mutator;

    @Override
    public String getName() {
        return String.format("%s - %s (1:n)", this.a.getName(), this.b.getName());
    }

    @Override
    public List<B> query(A a) {
        return this.accessor.apply(a);
    }

    @Override
    public A update(A a, B b) {
        return this.mutator.apply(a, b);
    }

    @Override
    public String toString() {
        return this.getName();
    }
}
