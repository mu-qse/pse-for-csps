package de.fhms.mu.pse.model.domain;

public interface IDomainType<T> {
    String getName();
    Class<T> getType();
}
