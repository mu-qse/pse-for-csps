package de.fhms.mu.pse.model.primitive;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

@RequiredArgsConstructor
@Getter
public class Pair<T> {
    private final T a;
    private final T b;

    public boolean isEqual() {
        return this.a.equals(this.b);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Pair<?> pair)) {
            return false;
        }
        return Objects.equals(this.a, pair.a) && Objects.equals(this.b, pair.b);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.a, this.b);
    }

    @Override
    public String toString() {
        return String.format("(%s, %s)", a, b);
    }
}
