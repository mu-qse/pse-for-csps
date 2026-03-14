package de.fhms.mu.pse.model.problem.encoding;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class EncodingVariable<TData, TValue> {
    private final String name;
    private final TData data;
    private TValue value;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof EncodingVariable<?, ?> that)) {
            return false;
        }
        return Objects.equals(this.name, that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.name);
    }

    @Override
    public String toString() {
        return this.getName();
    }
}
