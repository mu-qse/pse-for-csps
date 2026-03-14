package de.fhms.mu.pse.model.domain;

public interface IProblemDomainBuilder<T, I> {
    IProblemDomain<T> build(I input);
}
