package de.fhms.mu.pse.model.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AggregateRoot<T> extends Entity<T> implements IAggregateRoot<T> {
    private final IAggregateFactory<T> factory;

    public AggregateRoot(IAggregateFactory<T> factory) {
        super(factory.getType(), factory::create);

        this.factory = factory;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
