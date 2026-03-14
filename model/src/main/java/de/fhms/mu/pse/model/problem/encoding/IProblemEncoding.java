package de.fhms.mu.pse.model.problem.encoding;

import java.util.List;

public interface IProblemEncoding<T extends EncodingVariable<?, ?>> {
    String getName();
    void add(T variable);
    List<T> getVariables();
}
