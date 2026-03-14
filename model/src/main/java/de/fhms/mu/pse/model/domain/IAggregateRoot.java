package de.fhms.mu.pse.model.domain;

public interface IAggregateRoot<T> extends IEntity<T> {
    IAggregateFactory<T> getFactory();
}
