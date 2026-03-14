package de.fhms.mu.pse;

import de.fhms.mu.pse.annotations.domain.*;
import de.fhms.mu.pse.model.domain.*;
import de.fhms.mu.pse.model.domain.AggregateRoot;
import de.fhms.mu.pse.model.domain.Entity;
import de.fhms.mu.pse.model.domain.ValueObject;
import lombok.Data;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Data
public class AggregateProcessor<R> implements IProblemDomainBuilder<R, IAggregateFactory<R>> {
    private final Class<R> aggregateRootType;

    @Override
    public IProblemDomain<R> build(IAggregateFactory<R> aggregateFactory) {
        if (!this.aggregateRootType.isAnnotationPresent(de.fhms.mu.pse.annotations.domain.AggregateRoot.class)) {
            throw new IllegalArgumentException(String.format("Class must be annotated with @AggregateRoot: %s", this.aggregateRootType.getSimpleName()));
        }

        final var aggregateRoot = new AggregateRoot<>(aggregateFactory);
        final var domain = new DefaultProblemDomain<>(aggregateRoot);

        final var types = this.createDomainTypes(domain, aggregateRoot);
        types.forEach(domain::add);

        final var relations = this.createRelations(domain);
        relations.forEach(domain::add);

        return domain;
    }

    private Stream<IDomainType<?>> createDomainTypes(IProblemDomain<R> domain, IDomainType<?> parent) {
        return Stream.of(
                this.createTypesFromOneToManyRelationFields(domain, parent),
                this.createTypesFromOneToManyRelationMethods(domain, parent),
                this.createTypesFromManyToOneRelationFields(domain, parent),
                this.createTypesFromManyToOneRelationMethods(domain, parent)
        )
                .flatMap(stream -> stream)
                .flatMap(domainType -> Stream.concat(Stream.of(domainType), this.createDomainTypes(domain, domainType)));
    }

    private Stream<? extends IDomainType<?>> createTypesFromOneToManyRelationFields(IProblemDomain<R> domain, IDomainType<?> parent) {
        return Arrays.stream(parent.getType().getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(OneToMany.class))
                .filter(field -> {
                    final var type = field.getType();
                    return type.equals(List.class) || type.equals(Collection.class);
                })
                .map(field -> {
                    final var listType = (ParameterizedType) field.getGenericType();
                    return (Class<Object>) listType.getActualTypeArguments()[0];
                })
                .filter(type -> domain.getType(type).isEmpty())
                .map(type -> this.createDomainType(domain, type));
    }

    private Stream<? extends IDomainType<?>> createTypesFromOneToManyRelationMethods(IProblemDomain<R> domain, IDomainType<?> parent) {
        return Arrays.stream(parent.getType().getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(OneToMany.class))
                .filter(method -> {
                    final var type = method.getReturnType();
                    return type.equals(List.class) || type.equals(Collection.class);
                })
                .map(method -> {
                    final var listType = (ParameterizedType) method.getGenericReturnType();
                    return (Class<Object>) listType.getActualTypeArguments()[0];
                })
                .filter(type -> domain.getType(type).isEmpty())
                .map(type -> this.createDomainType(domain, type));
    }

    private Stream<? extends IDomainType<?>> createTypesFromManyToOneRelationFields(IProblemDomain<R> domain, IDomainType<?> parent) {
        return Arrays.stream(parent.getType().getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(ManyToOne.class))
                .map(field -> (Class<Object>) field.getType())
                .filter(type -> domain.getType(type).isEmpty())
                .map(type -> this.createDomainType(domain, type));
    }

    private Stream<? extends IDomainType<?>> createTypesFromManyToOneRelationMethods(IProblemDomain<R> domain, IDomainType<?> parent) {
        return Arrays.stream(parent.getType().getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(ManyToOne.class))
                .map(method -> (Class<Object>) method.getReturnType())
                .filter(type -> domain.getType(type).isEmpty())
                .map(type -> this.createDomainType(domain, type));
    }

    private IDomainType<?> createDomainType(IProblemDomain<R> domain, Class<Object> type) {
        if (type.isAnnotationPresent(de.fhms.mu.pse.annotations.domain.ValueObject.class)) {
            return this.createValueObject(type);
        }

        if (type.isAnnotationPresent(de.fhms.mu.pse.annotations.domain.Entity.class)) {
            return this.createEntity(type);
        }

        return createPrimitiveDomainType(type);
        // throw new IllegalArgumentException("Domain type must be annotated: " + type.getSimpleName());
    }

    private <T> ValueObject<T> createValueObject(Class<T> type) {
        final var createConstructor = Arrays.stream(type.getDeclaredConstructors())
                .filter(constructor -> type.isRecord() || constructor.getParameterCount() == 0 || constructor.isAnnotationPresent(Create.class))
                .findFirst()
                .map(constructor -> (Constructor<T>) constructor);

        return new ValueObject<>(type, args -> {
            if (createConstructor.isEmpty()) {
                throw new IllegalArgumentException("Missing @Create in class for value object: " + type.getSimpleName());
            }

            try {
                return createConstructor.get().newInstance(args);
            } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private <T> Entity<T> createEntity(Class<T> type) {
        final var createConstructor = Arrays.stream(type.getDeclaredConstructors())
                .filter(constructor -> constructor.getParameterCount() == 0 || constructor.isAnnotationPresent(Create.class))
                .findFirst()
                .map(constructor -> (Constructor<T>) constructor);

        return new Entity<>(type, args -> {
            if (createConstructor.isEmpty()) {
                throw  new IllegalArgumentException("Missing @Create in class for entity: " + type.getSimpleName());
            }

            try {
                return createConstructor.get().newInstance(args);
            } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static IDomainType<Object> createPrimitiveDomainType(Class<Object> type) {
        return new IDomainType<>() {
            @Override
            public String getName() {
                return type.getSimpleName();
            }

            @Override
            public Class<Object> getType() {
                return type;
            }
        };
    }

    private List<? extends IRelation<?, ?>> createRelations(IProblemDomain<R> domain) {
        final var domainTypes = domain.getTypes();
        return domainTypes.stream()
                .flatMap(domainType -> {
                    final var type = domainType.getType();
                    return Stream.of(
                            this.createOneToManyRelationsByFields(domain, domainType, type),
                            this.createOneToManyRelationsByMethods(domain, domainType, type),
                            this.createManyToOneRelationsByFields(domain, domainType, type),
                            this.createManyToOneRelationsByMethods(domain, domainType, type),
                            this.createOneToOneRelationsByFields(domain, domainType, type),
                            this.createOneToOneRelationsByMethods(domain, domainType, type)
                    ).flatMap(stream -> stream);
                })
                .toList();
    }

    private Stream<? extends IOneToManyRelation<?, Object>> createOneToManyRelationsByFields(IProblemDomain<R> domain, IDomainType<?> domainType, Class<?> type) {
        return Arrays.stream(type.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(OneToMany.class))
                .filter(field -> {
                    final var fieldType = field.getType();
                    return fieldType.equals(List.class) || fieldType.equals(Collection.class);
                })
                .map(field -> this.getOneToManyRelationByField(domain, domainType, field));
    }

    private <A, B> IOneToManyRelation<A, B> getOneToManyRelationByField(IProblemDomain<R> domain, IDomainType<A> domainType, Field field) {
        final var listType = (ParameterizedType) field.getGenericType();
        final var listTypeClass = (Class<?>) listType.getActualTypeArguments()[0];
        final var b = domain.getType((Class<B>) listTypeClass)
                .orElseThrow(() -> new IllegalArgumentException("Could not find domain-type: " + listTypeClass.getSimpleName()));

        final var accessor = this.getOneToManyQueryMethodForType(domainType, b.getType())
                .orElseThrow(() -> new IllegalArgumentException(String.format("Missing @Query for type: %s <> %s", domainType.getType().getSimpleName(), b.getType().getSimpleName())));
        final var mutator = this.getOneToManyUpdateMethodForType(domainType, b.getType());
                //.orElseThrow(() -> new IllegalArgumentException(String.format("Missing @Update for type: %s <> %s", domainType.getType().getSimpleName(), b.getType().getSimpleName())));

        return new DefaultOneToManyRelation<>(domainType, b, instance -> {
            try {
                return (List<B>) accessor.invoke(instance);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e.getCause());
            }
        }, (instance, value) -> {
            if (mutator.isEmpty()) {
                return instance;
            }

            try {
                return (A) mutator.get().invoke(instance, value);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e.getCause());
            }
        });
    }

    private Stream<? extends IOneToManyRelation<?, Object>> createOneToManyRelationsByMethods(IProblemDomain<R> domain, IDomainType<?> domainType, Class<?> type) {
        return Arrays.stream(type.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(OneToMany.class))
                .filter(method -> {
                    final var returnType = method.getReturnType();
                    return returnType.equals(List.class) || returnType.equals(Collection.class);
                })
                .map(method -> this.getOneToManyRelationByMethod(domain, domainType, method));
    }

    private <A, B> IOneToManyRelation<A, B> getOneToManyRelationByMethod(IProblemDomain<R> domain, IDomainType<A> domainType, Method method) {
        final var listType = (ParameterizedType) method.getGenericReturnType();
        final var listTypeClass = (Class<?>) listType.getActualTypeArguments()[0];
        final var b = domain.getType((Class<B>) listTypeClass)
                .orElseThrow(() -> new IllegalArgumentException("Could not find domain-type: " + listTypeClass.getSimpleName()));

        final var mutator = this.getOneToManyUpdateMethodForType(domainType, b.getType());
                //.orElseThrow(() -> new IllegalArgumentException(String.format("Missing @Update for relation: %s <> %s", domainType.getType().getSimpleName(), b.getType().getSimpleName())));

        return new DefaultOneToManyRelation<>(domainType, b, instance -> {
            try {
                return (List<B>) method.invoke(instance);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e.getCause());
            }
        }, (instance, value) -> {
            if (mutator.isEmpty()) {
                return instance;
            }

            try {
                return (A) mutator.get().invoke(instance, value);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e.getCause());
            }
        });
    }

    private <T, B> Optional<Method> getOneToManyQueryMethodForType(IDomainType<T> domainType, Class<B> b) {
        final var type = domainType.getType();
        return Arrays.stream(type.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(Query.class))
                .filter(method -> {
                    final var listType = (ParameterizedType) method.getGenericReturnType();
                    final var listTypeClass = (Class<?>) listType.getActualTypeArguments()[0];
                    return listTypeClass.equals(b);
                })
                .findFirst();
    }

    private <T, B> Optional<Method> getOneToManyUpdateMethodForType(IDomainType<T> domainType, Class<B> b) {
        final var type = domainType.getType();
        return Arrays.stream(type.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(Update.class))
                .filter(method -> {
                    final var returnType = method.getReturnType();
                    final var parameters = method.getParameters();
                    final var firstParameter = parameters[0];
                    final var parameterType = firstParameter.getType();
                    if (!parameterType.equals(b)) {
                        return false;
                    }
                    if (!returnType.equals(type)) {
                        throw new IllegalArgumentException(String.format("Invalid return type of method: %s %s()", returnType.getSimpleName(), method.getName()));
                    }

                    return true;
                })
                .findFirst();
    }

    private Stream<? extends IManyToOneRelation<?, Object>> createManyToOneRelationsByFields(IProblemDomain<R> domain, IDomainType<?> domainType, Class<?> type) {
        return Arrays.stream(type.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(ManyToOne.class))
                .map(field -> this.getManyToOneRelationByField(domain, domainType, field));
    }

    private <A, B> IManyToOneRelation<A, B> getManyToOneRelationByField(IProblemDomain<R> domain, IDomainType<A> domainType, Field field) {
        final var fieldType = field.getType();
        final var b = domain.getType((Class<B>) fieldType)
                .orElseThrow(() -> new IllegalArgumentException("Could not find domain-type: " + fieldType.getSimpleName()));
        final var accessor = this.getManyToOneQueryMethodForType(domainType, fieldType)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Missing @Query for field: %s %s", fieldType.getSimpleName(), field.getName())));
        final var mutator = this.getManyToOneUpdateMethodForType(domainType, fieldType)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Missing @Update for field: %s %s", fieldType.getSimpleName(), field.getName())));

        return new DefaultManyToOneRelation<>(domainType, b, instance -> {
            try {
                return (B) accessor.invoke(instance);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e.getCause());
            }
        }, (instance, value) -> {
            try {
                return (A) mutator.invoke(instance, value);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e.getCause());
            }
        });
    }

    private Stream<? extends IManyToOneRelation<?, Object>> createManyToOneRelationsByMethods(IProblemDomain<R> domain, IDomainType<?> domainType, Class<?> type) {
        return Arrays.stream(type.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(ManyToOne.class))
                .map(method -> this.getManyToOneRelationByMethod(domain, domainType, method));
    }

    private <A, B> IManyToOneRelation<A, B> getManyToOneRelationByMethod(IProblemDomain<R> domain, IDomainType<A> domainType, Method method) {
        final var returnType = method.getReturnType();
        final var b = domain.getType((Class<B>) returnType)
                .orElseThrow(() -> new IllegalArgumentException("Could not find domain-type: " + returnType.getSimpleName()));
        final var mutator = this.getManyToOneUpdateMethodForType(domainType, returnType)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Missing @Update for type: %s", domainType.getType().getSimpleName())));

        return new DefaultManyToOneRelation<>(domainType, b, instance -> {
            try {
                return (B) method.invoke(instance);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e.getCause());
            }
        }, (instance, value) -> {
            try {
                return (A) mutator.invoke(instance, value);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e.getCause());
            }
        });
    }

    private <T, B> Optional<Method> getManyToOneQueryMethodForType(IDomainType<T> domainType, Class<B> b) {
        final var type = domainType.getType();
        return Arrays.stream(type.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(Query.class)
                        && method.getReturnType().equals(b))
                .findFirst();
    }

    private <T, B> Optional<Method> getManyToOneUpdateMethodForType(IDomainType<T> domainType, Class<B> b) {
        final var type = domainType.getType();
        return Arrays.stream(type.getDeclaredMethods())
                .filter(method -> {
                    if (!method.isAnnotationPresent(Update.class)) {
                        return false;
                    }

                    final var returnType = method.getReturnType();
                    final var parameters = method.getParameters();
                    final var firstParameter = parameters[0];
                    final var parameterType = firstParameter.getType();
                    if (!parameterType.equals(b)) {
                        return false;
                    }
                    if (!returnType.equals(type)) {
                        throw new IllegalArgumentException(String.format("Invalid return type of method: %s %s(%s) - should be %s %s(%s)", returnType.getSimpleName(), method.getName(), parameterType.getSimpleName(), type.getSimpleName(), method.getName(), parameterType.getSimpleName()));
                    }

                    return true;
                })
                .findFirst();
    }

    private Stream<? extends IOneToOneRelation<?, Object>> createOneToOneRelationsByFields(IProblemDomain<R> domain, IDomainType<?> domainType, Class<?> type) {
        return Arrays.stream(type.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(OneToOne.class))
                .map(field -> this.getOneToOneRelationByField(domain, domainType, field));
    }

    private <A, B> IOneToOneRelation<A, B> getOneToOneRelationByField(IProblemDomain<R> domain, IDomainType<A> domainType, Field field) {
        final var fieldType = field.getType();
        final var b = domain.getType((Class<B>) fieldType)
                .orElseThrow(() -> new IllegalArgumentException("Could not find domain-type: " + fieldType.getSimpleName()));
        final var accessor = this.getOneToOneQueryMethodForType(domainType, fieldType)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Missing @Query for field: %s %s", fieldType.getSimpleName(), field.getName())));
        final var mutator = this.getOneToOneUpdateMethodForType(domainType, fieldType)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Missing @Update for field: %s %s", fieldType.getSimpleName(), field.getName())));

        return new DefaultOneToOneRelation<>(domainType, b, instance -> {
            try {
                return (B) accessor.invoke(instance);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e.getCause());
            }
        }, (instance, value) -> {
            try {
                return (A) mutator.invoke(instance, value);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e.getCause());
            }
        });
    }

    private Stream<? extends IOneToOneRelation<?, Object>> createOneToOneRelationsByMethods(IProblemDomain<R> domain, IDomainType<?> domainType, Class<?> type) {
        return Arrays.stream(type.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(OneToOne.class))
                .map(method -> this.getOneToOneRelationByMethod(domain, domainType, method));
    }

    private <A, B> IOneToOneRelation<A, B> getOneToOneRelationByMethod(IProblemDomain<R> domain, IDomainType<A> domainType, Method method) {
        final var returnType = method.getReturnType();
        final var b = domain.getType((Class<B>) returnType)
                .orElseThrow(() -> new IllegalArgumentException("Could not find domain-type: " + returnType.getSimpleName()));
        final var mutator = this.getOneToOneUpdateMethodForType(domainType, returnType)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Missing @Update for type: %s", domainType.getType().getSimpleName())));

        return new DefaultOneToOneRelation<>(domainType, b, instance -> {
            try {
                return (B) method.invoke(instance);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e.getCause());
            }
        }, (instance, value) -> {
            try {
                return (A) mutator.invoke(instance, value);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e.getCause());
            }
        });
    }

    private <T, B> Optional<Method> getOneToOneQueryMethodForType(IDomainType<T> domainType, Class<B> b) {
        final var type = domainType.getType();
        return Arrays.stream(type.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(Query.class)
                        && method.getReturnType().equals(b))
                .findFirst();
    }

    private <T, B> Optional<Method> getOneToOneUpdateMethodForType(IDomainType<T> domainType, Class<B> b) {
        final var type = domainType.getType();
        return Arrays.stream(type.getDeclaredMethods())
                .filter(method -> {
                    if (!method.isAnnotationPresent(Update.class)) {
                        return false;
                    }

                    final var returnType = method.getReturnType();
                    final var parameters = method.getParameters();
                    final var firstParameter = parameters[0];
                    final var parameterType = firstParameter.getType();
                    if (!parameterType.equals(b)) {
                        return false;
                    }
                    if (!returnType.equals(type)) {
                        throw new IllegalArgumentException(String.format("Invalid return type of method: %s %s(%s) - should be %s %s(%s)", returnType.getSimpleName(), method.getName(), parameterType.getSimpleName(), type.getSimpleName(), method.getName(), parameterType.getSimpleName()));
                    }

                    return true;
                })
                .findFirst();
    }

    /*private <E> SolutionElement<E> createSolutionElement(Class<E> type, Class<?>) throws NoSuchMethodException {
        final var noArgsConstructor = type.getConstructor();
        final var copyConstructor = type.getConstructor(type);
        return new SolutionElement<>(type, () -> {
            try {
                return noArgsConstructor.newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e.getCause());
            }
        }, other -> {
            try {
                return copyConstructor.newInstance(other);
            } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e.getCause());
            }
        });
    }*/
}
