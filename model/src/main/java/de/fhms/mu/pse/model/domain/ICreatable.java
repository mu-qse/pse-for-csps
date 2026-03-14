package de.fhms.mu.pse.model.domain;

public interface ICreatable<T> {
    T create(Object... args);
}
