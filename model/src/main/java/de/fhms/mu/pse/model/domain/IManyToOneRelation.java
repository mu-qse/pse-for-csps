package de.fhms.mu.pse.model.domain;

public interface IManyToOneRelation<A, B> extends IRelation<A, B> {
    A update(A a, B b);
    B query(A a);
}
