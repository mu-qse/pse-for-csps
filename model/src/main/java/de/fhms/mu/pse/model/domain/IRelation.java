package de.fhms.mu.pse.model.domain;

public interface IRelation<A, B> {
    String getName();
    IDomainType<A> getA();
    IDomainType<B> getB();
}
