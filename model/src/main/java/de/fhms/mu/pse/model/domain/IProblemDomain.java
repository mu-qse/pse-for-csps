package de.fhms.mu.pse.model.domain;

import java.util.List;
import java.util.Optional;

public interface IProblemDomain<S> {
    IAggregateRoot<S> getAggregateRoot();
    List<IDomainType<?>> getTypes();
    List<IRelation<?, ?>> getRelations();
    <A> List<IRelation<A, Object>> getRelations(IDomainType<A> domainType);
    <A> List<IRelation<A, Object>> getRelations(Class<A> type);
    <A> List<IOneToManyRelation<A, Object>> getOneToManyRelations(IDomainType<A> type);
    <A> List<IOneToManyRelation<A, Object>> getOneToManyRelations(Class<A> type);
    <A> List<IManyToOneRelation<A, Object>> getManyToOneRelations(IDomainType<A> type);
    <A> List<IManyToOneRelation<A, Object>> getManyToOneRelations(Class<A> type);
    <A> List<IOneToOneRelation<A, Object>> getOneToOneRelations(IDomainType<A> type);
    <A> List<IOneToOneRelation<A, Object>> getOneToOneRelations(Class<A> type);

    <T> Optional<IDomainType<T>> getType(Class<T> type);
    <A, B> Optional<IRelation<A, B>> getRelation(IDomainType<A> a, IDomainType<B> b);
    <A, B> Optional<IRelation<A, B>> getRelation(Class<A> a, Class<B> b);
    <A, B> Optional<IManyToOneRelation<A, B>> getManyToOneRelation(IDomainType<A> a, IDomainType<B> b);
    <A, B> Optional<IManyToOneRelation<A, B>> getManyToOneRelation(Class<A> a, Class<B> b);
    <A, B> Optional<IOneToManyRelation<A, B>> getOneToManyRelation(IDomainType<A> a, IDomainType<B> b);
    <A, B> Optional<IOneToManyRelation<A, B>> getOneToManyRelation(Class<A> a, Class<B> b);

}
