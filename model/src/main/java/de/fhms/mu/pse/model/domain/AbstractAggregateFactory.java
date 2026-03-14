package de.fhms.mu.pse.model.domain;

import lombok.Data;

@Data
public abstract class AbstractAggregateFactory<T> implements IAggregateFactory<T> {
    private final Class<T> type;
}
