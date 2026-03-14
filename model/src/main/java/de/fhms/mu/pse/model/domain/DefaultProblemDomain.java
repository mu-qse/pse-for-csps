package de.fhms.mu.pse.model.domain;

import lombok.Data;

import java.util.*;
import java.util.stream.Collectors;

@Data
public class DefaultProblemDomain<S> implements IProblemDomain<S> {
    private final IAggregateRoot<S> aggregateRoot;
    private final Map<Class<?>, IDomainType<?>> types = new HashMap<>();
    private final Map<IRelation<?, ?>, IRelation<?, ?>> relations = new HashMap<>();

    public DefaultProblemDomain(IAggregateRoot<S> aggregateRoot) {
        this.aggregateRoot = aggregateRoot;
        this.add(aggregateRoot);
    }

    public DefaultProblemDomain<S> add(IDomainType<?> type) {
        if (this.types.containsKey(type.getType())) {
            //throw new IllegalArgumentException(type + " already exists");
            return this;
        }

        this.types.put(type.getType(), type);
        return this;
    }

    public DefaultProblemDomain<S> add(IRelation<?, ?> relation) {
        if (this.relations.containsKey(relation)) {
            throw new IllegalArgumentException(relation + " already exists");
        }

        this.relations.put(relation, relation);

        final var a = relation.getA();
        if (!this.types.containsKey(a.getType())) {
            this.add(a);
        }

        final var b = relation.getB();
        if (!this.types.containsKey(b.getType())) {
            this.add(b);
        }

        return this;
    }

    @Override
    public List<IDomainType<?>> getTypes() {
        return new ArrayList<>(this.types.values());
    }

    @Override
    public List<IRelation<?, ?>> getRelations() {
        return new ArrayList<>(this.relations.values());
    }

    @Override
    public <A> List<IRelation<A, Object>> getRelations(IDomainType<A> type) {
        return this.relations.values().stream()
                .filter(relation -> relation.getA().equals(type))
                .map(relation -> (IRelation<A, Object>) relation)
                .toList();
    }

    @Override
    public <A> List<IRelation<A, Object>> getRelations(Class<A> type) {
        return this.relations.values().stream()
                .filter(relation -> relation.getA().getType().equals(type))
                .map(relation -> (IRelation<A, Object>) relation)
                .toList();
    }

    @Override
    public <A> List<IOneToManyRelation<A, Object>> getOneToManyRelations(IDomainType<A> type) {
        return this.getRelations(type).stream()
                .filter(relation -> relation instanceof IOneToManyRelation<?, ?>)
                .map(relation -> (IOneToManyRelation<A, Object>) relation)
                .toList();
    }

    @Override
    public <A> List<IOneToManyRelation<A, Object>> getOneToManyRelations(Class<A> type) {
        return this.getRelations(type).stream()
                .filter(relation -> relation instanceof IOneToManyRelation<?, ?>)
                .map(relation -> (IOneToManyRelation<A, Object>) relation)
                .toList();
    }

    @Override
    public <A> List<IManyToOneRelation<A, Object>> getManyToOneRelations(IDomainType<A> type) {
        return this.getRelations(type).stream()
                .filter(relation -> relation instanceof IManyToOneRelation<?, ?>)
                .filter(relation -> !(relation instanceof IOneToOneRelation<?, ?>))
                .map(relation -> (IManyToOneRelation<A, Object>) relation)
                .toList();
    }

    @Override
    public <A> List<IManyToOneRelation<A, Object>> getManyToOneRelations(Class<A> type) {
        return this.getRelations(type).stream()
                .filter(relation -> relation instanceof IManyToOneRelation<?, ?>)
                .filter(relation -> !(relation instanceof IOneToOneRelation<?, ?>))
                .map(relation -> (IManyToOneRelation<A, Object>) relation)
                .toList();
    }

    @Override
    public <A> List<IOneToOneRelation<A, Object>> getOneToOneRelations(IDomainType<A> type) {
        return this.getRelations(type).stream()
                .filter(relation -> relation instanceof IOneToOneRelation<?, ?>)
                .map(relation -> (IOneToOneRelation<A, Object>) relation)
                .toList();
    }

    @Override
    public <A> List<IOneToOneRelation<A, Object>> getOneToOneRelations(Class<A> type) {
        return this.getRelations(type).stream()
                .filter(relation -> relation instanceof IOneToOneRelation<?, ?>)
                .map(relation -> (IOneToOneRelation<A, Object>) relation)
                .toList();
    }

    @Override
    public <T> Optional<IDomainType<T>> getType(Class<T> type) {
        return Optional.ofNullable((IDomainType<T>) this.types.get(type));
    }

    @Override
    public <A, B> Optional<IRelation<A, B>> getRelation(IDomainType<A> a, IDomainType<B> b) {
        return this.relations.values().stream()
                .filter(relation -> relation.getA().equals(a) && relation.getB().equals(b))
                .map(relation -> (IRelation<A, B>) relation)
                .findFirst();
                //.orElseThrow(() -> new IllegalArgumentException(String.format("Unknown relation between %s and %s", a.getSimpleName(), b.getSimpleName())));
    }

    @Override
    public <A, B> Optional<IRelation<A, B>> getRelation(Class<A> a, Class<B> b) {
        return this.relations.values().stream()
                .filter(relation -> relation.getA().getType().equals(a) && relation.getB().getType().equals(b))
                .map(relation -> (IRelation<A, B>) relation)
                .findFirst();
                //.orElseThrow(() -> new IllegalArgumentException(String.format("Unknown relation between %s and %s", a.getSimpleName(), b.getSimpleName())));
    }

    @Override
    public <A, B> Optional<IManyToOneRelation<A, B>> getManyToOneRelation(IDomainType<A> a, IDomainType<B> b) {
        return this.relations.values().stream()
                .filter(relation -> relation instanceof IManyToOneRelation<?, ?>
                        && relation.getA().equals(a) && relation.getB().equals(b))
                .map(relation -> (IManyToOneRelation<A, B>) relation)
                .findFirst();
                //.orElseThrow(() -> new IllegalArgumentException(String.format("Unknown many-to-one relation between %s and %s", a.getSimpleName(), b.getSimpleName())));
    }

    @Override
    public <A, B> Optional<IManyToOneRelation<A, B>> getManyToOneRelation(Class<A> a, Class<B> b) {
        return this.relations.values().stream()
                .filter(relation -> relation instanceof IManyToOneRelation<?, ?> && relation.getA().getType().equals(a) && relation.getB().getType().equals(b))
                .map(relation -> (IManyToOneRelation<A, B>) relation)
                .findFirst();
                //.orElseThrow(() -> new IllegalArgumentException(String.format("Unknown many-to-one relation between %s and %s", a.getSimpleName(), b.getSimpleName())));
    }

    @Override
    public <A, B> Optional<IOneToManyRelation<A, B>> getOneToManyRelation(IDomainType<A> a, IDomainType<B> b) {
        return this.relations.values().stream()
                .filter(relation -> relation instanceof IOneToManyRelation<?, ?>
                        && relation.getA().equals(a) && relation.getB().equals(b))
                .map(relation -> (IOneToManyRelation<A, B>) relation)
                .findFirst();
                //.orElseThrow(() -> new IllegalArgumentException(String.format("Unknown one-to-many relation between %s and %s", a.getSimpleName(), b.getSimpleName())));
    }

    @Override
    public <A, B> Optional<IOneToManyRelation<A, B>> getOneToManyRelation(Class<A> a, Class<B> b) {
        return this.relations.values().stream()
                .filter(relation -> relation instanceof IOneToManyRelation<?, ?>
                        && relation.getA().getType().equals(a) && relation.getB().getType().equals(b))
                .map(relation -> (IOneToManyRelation<A, B>) relation)
                .findFirst();
        //.orElseThrow(() -> new IllegalArgumentException(String.format("Unknown one-to-many relation between %s and %s", a.getSimpleName(), b.getSimpleName())));
    }

    @Override
    public String toString() {
        return String.join("\n",
                !this.types.isEmpty() ? String.format("Domain types: \n%s", this.types.values().stream()
                        .map(type -> String.format("- %s", type.getName()))
                        .collect(Collectors.joining("\n"))) : "Domain types: None",
                !this.relations.isEmpty() ? String.format("Relations: \n%s", this.relations.values().stream()
                        .map(relation -> String.format("- %s", relation))
                        .collect(Collectors.joining("\n"))) : "Relations: None"
        );
    }
}
