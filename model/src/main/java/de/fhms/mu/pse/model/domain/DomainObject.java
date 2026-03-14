package de.fhms.mu.pse.model.domain;

import lombok.Data;

import java.util.Objects;

@Data
public abstract class DomainObject<T> implements IDomainType<T> {
    private final Class<T> type;

    public DomainObject(Class<T> type) {
        this.type = type;
    }

    @Override
    public String getName() {
        return this.type.getSimpleName();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DomainObject<?> that)) {
            return false;
        }
        return Objects.equals(this.type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.type);
    }

    @Override
    public String toString() {
        return this.getName();
    }
}
