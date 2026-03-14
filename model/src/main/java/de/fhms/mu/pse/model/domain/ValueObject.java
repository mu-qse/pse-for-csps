package de.fhms.mu.pse.model.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.function.Function;

@Data
@EqualsAndHashCode(callSuper = true)
public class ValueObject<T> extends DomainObject<T> implements IValueObject<T> {
    private final Function<Object, T> createFunction;

    public ValueObject(Class<T> type, Function<Object, T> createFunction) {
        super(type);

        this.createFunction = createFunction;
    }

    @Override
    public T create(Object... args) {
        return this.createFunction.apply(args);
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
