package de.fhms.mu.pse.model.problem.encoding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class ProblemEncoding<
        T extends EncodingVariable<?, ?>
> implements IProblemEncoding<
        T
> {
    private final List<T> variables = new ArrayList<>();

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void add(T variable) {
        this.variables.add(variable);
    }

    @Override
    public List<T> getVariables() {
        return Collections.unmodifiableList(this.variables);
    }
}
