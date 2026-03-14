package de.fhms.mu.pse.model.domain;

import java.util.List;

public interface IOneToManyRelation<A, B> extends IRelation<A, B> {
    A update(A a, B b);
    List<B> query(A a);
}
