package de.fhms.mu.pse.model.domain;

public interface IAggregateFactory<T> {
    Class<T> getType();
    T create(Object... args);
}
