package de.fhms.mu.pse;

import de.fhms.mu.pse.annotations.problem.SelectableElements;
import de.fhms.mu.pse.annotations.problem.constraint.LogicConstraint;
import de.fhms.mu.pse.annotations.problem.constraint.ProblemConstraint;
import de.fhms.mu.pse.model.domain.*;
import de.fhms.mu.pse.model.problem.CombinatorialProblemElementSet;
import de.fhms.mu.pse.model.problem.DefaultCombinatorialProblem;
import de.fhms.mu.pse.model.problem.ICombinatorialProblem;
import de.fhms.mu.pse.model.problem.constraint.ICombinatorialProblemConstraint;
import de.fhms.mu.pse.model.problem.constraint.ISolutionElementConstraint;
import de.fhms.mu.pse.model.problem.constraint.logic.SolutionElementLogicConstraint;
import de.fhms.mu.pse.model.problem.constraint.logic.SolutionElementPairLogicConstraint;
import lombok.Data;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Data
public class CombinatorialProblemProcessor<S, E> implements ICombinatorialProblemFactory<IAggregateFactory<S>, S, E> {
    private final Class<S> aggregateRootType;
    private final Class<E> solutionElementType;

    public CombinatorialProblemProcessor(Class<S> aggregateRootType, Class<E> solutionElementType) {
        this.aggregateRootType = aggregateRootType;
        this.solutionElementType = solutionElementType;
    }

    @Override
    public ICombinatorialProblem<S, E> build(String name, IAggregateFactory<S> aggregateFactory) {
        final var domainProcessor = new AggregateProcessor<>(this.aggregateRootType);
        final var domain = domainProcessor.build(aggregateFactory);

        final var solutionElementDomainType = domain.getType(this.solutionElementType).orElseThrow();
        final var solutionElementRelations = Stream.concat(
                domain.getManyToOneRelations(solutionElementDomainType).stream(),
                domain.getOneToOneRelations(solutionElementDomainType).stream()
        ).toList();
        final var solutionElement = new ElementCombinationTuple<>(solutionElementDomainType, solutionElementRelations);
        final var combinatorialProblem = new DefaultCombinatorialProblem<>(name, domain, solutionElement);

        final var selectableElementsBySolutionAggregate = this.createSelectableElementsBySolutionAggregate(domain);
        selectableElementsBySolutionAggregate.forEach(combinatorialProblem::addSelectableElements);

        final var constraints = this.createConstraints(combinatorialProblem);
        constraints.forEach(combinatorialProblem::addConstraint);

        return combinatorialProblem;
    }

    private List<CombinatorialProblemElementSet<?>> createSelectableElementsBySolutionAggregate(IProblemDomain<S> domain) {
        final var selectableElements = new ArrayList<CombinatorialProblemElementSet<?>>();

        final var solutionAggregateRoot = domain.getAggregateRoot();
        final var solutionAggregateRootType = solutionAggregateRoot.getType();
        final var aggregateFactory = solutionAggregateRoot.getFactory();
        final var emptySolution = aggregateFactory.create();

        Arrays.stream(solutionAggregateRootType.getDeclaredMethods())
                .filter(field -> field.isAnnotationPresent(SelectableElements.class))
                .filter(method -> {
                    final var returnType = method.getReturnType();
                    return returnType.equals(List.class) || returnType.equals(Collection.class);
                })
                .map(method -> {
                    final var listType = (ParameterizedType) method.getGenericReturnType();
                    final var listTypeClass = (Class<?>) listType.getActualTypeArguments()[0];
                    final var domainType = domain.getType(listTypeClass).orElseThrow();
                    return this.createSelectableElements(domainType, emptySolution, method);
                })
                .forEach(selectableElements::add);

        return selectableElements;
    }

    private <T> CombinatorialProblemElementSet<?> createSelectableElements(IDomainType<T> type, Object instance, Method method) {
        final var elementSetAnnotation = method.getAnnotation(SelectableElements.class);

        try {
            final var elements = (List<T>) method.invoke(instance);
            final var name = !elementSetAnnotation.value().isEmpty() ? elementSetAnnotation.value() : type.getName();
            return new CombinatorialProblemElementSet<>(name, type, elements);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e.getCause());
        }
    }

    private List<? extends ICombinatorialProblemConstraint<S, E>> createConstraints(ICombinatorialProblem<S, E> problem) {
        final var constraints = new ArrayList<ICombinatorialProblemConstraint<S, E>>();

        Arrays.stream(this.aggregateRootType.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(ProblemConstraint.class))
                .map(method -> this.createConstraintByAggregateRootMethod(problem, method))
                .forEach(constraints::add);

        Arrays.stream(this.aggregateRootType.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(LogicConstraint.class))
                .map(method -> this.createLogicConstraintByAggregateRootMethod(problem, method))
                .forEach(constraints::add);

        Arrays.stream(this.solutionElementType.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(LogicConstraint.class))
                .map(method -> this.createLogicConstraintBySolutionElementMethod(problem, method))
                .forEach(constraints::add);

        return constraints;
    }

    private ICombinatorialProblemConstraint<S, E> createConstraintByAggregateRootMethod(ICombinatorialProblem<S, E> problem, Method method) {
        final var domain = problem.getDomain();
        final var solutionAggregateRoot = domain.getAggregateRoot();
        final var aggregateFactory = solutionAggregateRoot.getFactory();
        final var emptySolution = aggregateFactory.create();

        final var constraintAnnotation = method.getAnnotation(ProblemConstraint.class);

        final var parameters = method.getParameters();
        final var returnType = method.getReturnType();
        if (returnType.equals(ICombinatorialProblemConstraint.class)
                || returnType.equals(ISolutionElementConstraint.class)) {
            final ICombinatorialProblemConstraint<S, E> constraint;

            final var firstParameter = parameters[0];
            if (firstParameter.getType().equals(ICombinatorialProblem.class)) {
                try {
                    constraint = (ICombinatorialProblemConstraint<S, E>) method.invoke(emptySolution, problem);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e.getCause());
                }
            } else if (firstParameter.getType().equals(IProblemDomain.class)) {
                try {
                    constraint = (ICombinatorialProblemConstraint<S, E>) method.invoke(emptySolution, domain);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e.getCause());
                }
            } else if (firstParameter.getType().equals(IOneToManyRelation.class)) {
                final var relationType = (ParameterizedType) firstParameter.getParameterizedType();
                final var a = (Class<?>) relationType.getActualTypeArguments()[0];
                final var b = (Class<?>) relationType.getActualTypeArguments()[1];
                final var relation = domain.getOneToManyRelation(a, b);
                try {
                    constraint = (ICombinatorialProblemConstraint<S, E>) method.invoke(emptySolution, relation);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e.getCause());
                }
            } else if (firstParameter.getType().equals(IManyToOneRelation.class)) {
                final var relationType = (ParameterizedType) firstParameter.getParameterizedType();
                final var a = (Class<?>) relationType.getActualTypeArguments()[0];
                final var b = (Class<?>) relationType.getActualTypeArguments()[1];
                final var relation = domain.getManyToOneRelation(a, b);
                try {
                    constraint = (ICombinatorialProblemConstraint<S, E>) method.invoke(emptySolution, relation);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e.getCause());
                }
            } else {
                throw new IllegalArgumentException("Unknown parameter type for method annotated with @ProblemConstraint: " + returnType.getSimpleName());
            }

            final var annotatedWeight = constraintAnnotation.weight();
            if (annotatedWeight != 0.0) {
                constraint.setWeight(annotatedWeight);
            }

            return constraint;
        }

        throw new IllegalArgumentException("Unknown return type of method annotated with @ProblemConstraint: " + returnType.getSimpleName());
    }

    private ICombinatorialProblemConstraint<S, E> createLogicConstraintByAggregateRootMethod(ICombinatorialProblem<S, E> problem, Method method) {
        final var domain = problem.getDomain();
        final var solutionAggregateRoot = domain.getAggregateRoot();
        final var aggregateFactory = solutionAggregateRoot.getFactory();
        final var emptySolution = aggregateFactory.create();
        final var solutionElement = problem.getSolutionElementPrototype();

        final var constraintAnnotation = method.getAnnotation(LogicConstraint.class);

        final var parameters = method.getParameters();
        final var returnType = method.getReturnType();
        if (isBooleanReturnType(returnType)) {
            if (parameters.length == 1) {
                final var parameter = parameters[0];
                if (!parameter.getType().equals(this.solutionElementType)) {
                    throw new IllegalArgumentException(String.format("Unknown parameter type for @LogicConstraint method: %s(%s)", method.getName(), parameter.getType().getSimpleName()));
                }

                final var name = !constraintAnnotation.name().isEmpty() ? constraintAnnotation.name() : method.getName();
                final var constraint = new SolutionElementLogicConstraint<S, E>(name, solutionElement, element -> {
                    try {
                        final var result = (boolean) method.invoke(emptySolution, element);
                        return constraintAnnotation.inverted() != result;
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                });

                final var annotatedWeight = constraintAnnotation.weight();
                if (annotatedWeight != 0.0) {
                    constraint.setWeight(annotatedWeight);
                }

                return constraint;
            }

            if (parameters.length == 2) {
                final var firstParameter = parameters[0];
                final var secondParameter = parameters[1];
                if (!firstParameter.getType().equals(secondParameter.getType())
                        || !firstParameter.getType().equals(this.solutionElementType)) {
                    throw new IllegalArgumentException(String.format("Unknown parameter types for @LogicConstraint method: %s(%s,%s)", method.getName(), firstParameter.getType().getSimpleName(), secondParameter.getType().getSimpleName()));
                }

                final var name = !constraintAnnotation.name().isEmpty() ? constraintAnnotation.name() : method.getName();
                final var constraint = new SolutionElementPairLogicConstraint<S, E>(name, solutionElement, (a, b) -> {
                    try {
                        final var result = (boolean) method.invoke(emptySolution, a, b);
                        return constraintAnnotation.inverted() != result;
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                });

                final var annotatedWeight = constraintAnnotation.weight();
                if (annotatedWeight != 0.0) {
                    constraint.setWeight(annotatedWeight);
                }

                return constraint;
            }

            throw new IllegalArgumentException("Illegal number of parameters of method annotated with @LogicConstraint: " + Arrays.toString(parameters));
        }

        throw new IllegalArgumentException("Unknown return type of method annotated with @LogicConstraint: " + returnType.getSimpleName());
    }

    private ICombinatorialProblemConstraint<S, E> createLogicConstraintBySolutionElementMethod(ICombinatorialProblem<S, E> problem, Method method) {
        final var solutionElement = problem.getSolutionElementPrototype();

        final var constraintAnnotation = method.getAnnotation(LogicConstraint.class);

        final var parameters = method.getParameters();
        final var returnType = method.getReturnType();
        if (isBooleanReturnType(returnType)) {
            final ICombinatorialProblemConstraint<S, E> constraint;

            if (parameters.length == 0) {
                final var name = !constraintAnnotation.name().isEmpty() ? constraintAnnotation.name() : method.getName();
                final Predicate<E> predicate = element -> {
                    try {
                        final var result = (boolean) method.invoke(element);
                        return constraintAnnotation.inverted() != result;
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                };
                constraint = new SolutionElementLogicConstraint<>(name, solutionElement, predicate);
            } else if (parameters.length == 1) {
                final var parameter = parameters[0];
                if (!parameter.getType().equals(this.solutionElementType)) {
                    throw new IllegalArgumentException(String.format("Unknown parameter type for @LogicConstraint method: %s(%s)", method.getName(), parameter.getType().getSimpleName()));
                }

                final var name = !constraintAnnotation.name().isEmpty() ? constraintAnnotation.name() : method.getName();
                final BiPredicate<E, E> predicate = (a, b) -> {
                    try {
                        final var result = (boolean) method.invoke(a, b);
                        return constraintAnnotation.inverted() != result;
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                };
                constraint = new SolutionElementPairLogicConstraint<>(name, solutionElement, predicate);
            } else {
                throw new IllegalArgumentException("Illegal number of parameters of method annotated with @LogicConstraint: " + Arrays.toString(parameters));
            }

            final var annotatedWeight = constraintAnnotation.weight();
            if (annotatedWeight != 0.0) {
                constraint.setWeight(annotatedWeight);
            }

            return constraint;
        }

        throw new IllegalArgumentException("Unknown return type of method annotated with @LogicConstraint: " + returnType.getSimpleName());
    }

    private static boolean isBooleanReturnType(Class<?> returnType) {
        return returnType.equals(Boolean.class) || returnType.equals(Boolean.TYPE);
    }
}
