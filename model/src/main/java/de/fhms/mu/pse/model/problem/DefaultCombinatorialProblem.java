package de.fhms.mu.pse.model.problem;

import de.fhms.mu.pse.model.domain.*;
import de.fhms.mu.pse.model.problem.constraint.ConstraintViolationException;
import de.fhms.mu.pse.model.problem.constraint.ICombinatorialProblemConstraint;
import lombok.Data;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

@Data
public class DefaultCombinatorialProblem<S, E> implements ICombinatorialProblem<S, E> {
    private final String name;
    private final IProblemDomain<S> domain;
    private final ElementCombinationTuple<E> solutionElementPrototype;

    private final List<CombinatorialProblemElementSet<?>> selectableElements = new ArrayList<>();
    private final List<ICombinatorialProblemConstraint<S, E>> constraints = new ArrayList<>();

    @Override
    public IAggregateRoot<S> getSolutionAggregateRoot() {
        return this.domain.getAggregateRoot();
    }

    @Override
    public List<CombinatorialProblemElementSet<?>> getSelectableElements() {
        return Collections.unmodifiableList(this.selectableElements);
    }

    @Override
    public Optional<CombinatorialProblemElementSet<?>> getSelectableElements(String name) {
        return this.selectableElements.stream()
                .filter(element -> element.getName().equals(name))
                .findFirst();
    }

    @Override
    public <V> Optional<CombinatorialProblemElementSet<V>> getSelectableElements(Class<V> type) {
        return this.selectableElements.stream()
                .filter(elementSet -> elementSet.getType().equals(type))
                .map(elementSet -> (CombinatorialProblemElementSet<V>) elementSet)
                .findFirst();
    }

    @Override
    public <V> Optional<CombinatorialProblemElementSet<V>> getSelectableElements(IDomainType<V> type) {
        return this.selectableElements.stream()
                .filter(elementSet -> elementSet instanceof CombinatorialProblemElementSet<?>)
                .map(elementSet -> (CombinatorialProblemElementSet<?>) elementSet)
                .filter(elementSet -> elementSet.getReference().equals(type))
                .map(elementSet -> (CombinatorialProblemElementSet<V>) elementSet)
                .findFirst();
    }

    @Override
    public ICombinatorialProblem<S, E> addSelectableElements(CombinatorialProblemElementSet<?> elementSet) {
        this.selectableElements.add(elementSet);
        return this;
    }

    @Override
    public ICombinatorialProblem<S, E> addConstraint(ICombinatorialProblemConstraint<S, E> constraint) {
        this.constraints.add(constraint);
        return this;
    }

    @Override
    public List<ElementCombinationTuple<E>> getPossibleCombinations() {
        return this.combineElementsFromRelations();
    }

    @Override
    public S createSolution(List<ElementCombinationTuple<E>> combinationTuples) {
        final var solutionAggregateRoot = this.getSolutionAggregateRoot();
        final var solution = solutionAggregateRoot.create();
        final var solutionAggregateRootElementRelation = this.getSolutionAggregateRootElementRelation();
        combinationTuples.stream()
                .map(this::createSolutionElement)
                .forEach(element -> solutionAggregateRootElementRelation.update(solution, element));
        return solution;
    }

    @Override
    public E createSolutionElement(ElementCombinationTuple<E> combinationTuple) {
        final var relations = combinationTuple.getRelations();
        final var solutionElementDomainType = combinationTuple.getDomainType();

        var element = combinationTuple.getValue(solutionElementDomainType).orElseGet(() -> {
            try {
                return solutionElementDomainType.getType().getConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        });

        for (final var relation : relations) {
            final var value = combinationTuple.getValue(relation.getB());
            if (value.isEmpty()) {
                continue;
            }

            element = relation.update(element, value.get());
        }

        return element;
    }

    @Override
    public boolean isFeasible(S solution) {
        final var solutionAggregateRootElementRelation = this.getSolutionAggregateRootElementRelation();
        final var solutionElements = solutionAggregateRootElementRelation.query(solution);
        return this.constraints.stream()
                .allMatch(constraint -> constraint.isSatisfied(this, solution, solutionElements));
    }

    @Override
    public void verify(S solution) throws ConstraintViolationException {
        final var solutionAggregateRootElementRelation = this.getSolutionAggregateRootElementRelation();
        final var solutionElements = solutionAggregateRootElementRelation.query(solution);
        for (final var constraint : this.constraints) {
            if (!constraint.isSatisfied(this, solution, solutionElements)) {
                throw new ConstraintViolationException(constraint);
            }
        }
    }

    @Override
    public String toString() {
        return String.join("\n",
                String.format("Name: %s", this.getName()),
                String.format("%s", this.getDomain()),
                String.format("Solution aggregate: %s", this.getSolutionAggregateRoot()),
                String.format("Solution element: %s", this.getSolutionElementPrototype()),
                !this.selectableElements.isEmpty() ? String.format("Selectable elements: \n%s", this.selectableElements.stream()
                        .map(variable -> String.format("- %s", variable))
                        .collect(Collectors.joining("\n"))) : "Selectable elements: None",
                !this.constraints.isEmpty() ? String.format("Constraints: \n%s", this.constraints.stream()
                        .map(constraint -> String.format("- %s (Weight: %.3f)", constraint, constraint.getWeight()))
                        .collect(Collectors.joining("\n"))) : "Constraints: None"
        );
    }

    private IOneToManyRelation<S, E> getSolutionAggregateRootElementRelation() {
        final var solutionAggregateRoot = this.getSolutionAggregateRoot();
        final var solutionElementDomainType = this.solutionElementPrototype.getDomainType();
        return this.domain.getOneToManyRelation(solutionAggregateRoot, solutionElementDomainType)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Could not find one-to-many relation: %s -> %s", solutionAggregateRoot, solutionElementDomainType)));
    }

    private List<ElementCombinationTuple<E>> combineElementsFromRelations() {
        final var relations = this.solutionElementPrototype.getRelations();

        var result = new ArrayList<>(this.getVariationsForSolutionElement());

        for (final var relation : relations) {
            final var variations = this.getVariationsForRelation(relation);
            final var newResult = new ArrayList<ElementCombinationTuple<E>>();
            for (final var base : result) {
                for (final var partial : variations) {
                    final var copy = new ElementCombinationTuple<>(base);
                    copy.fillValues(partial);
                    newResult.add(copy);
                }
            }

            result = newResult;
        }

        return result;
    }

    private List<ElementCombinationTuple<E>> getVariationsForSolutionElement() {
        final var domainType = this.solutionElementPrototype.getDomainType();
        final var elementVariable = this.getSelectableElements(domainType);
        return elementVariable.map(variable -> variable.get().stream()
                .map(element -> {
                    final var instance = new ElementCombinationTuple<>(this.solutionElementPrototype);
                    instance.setValue(domainType, element);
                    return instance;
                })
                .toList()
        ).orElse(List.of(this.solutionElementPrototype));
    }

    private <B> List<ElementCombinationTuple<E>> getVariationsForRelation(IManyToOneRelation<E, B> relation) {
        final var elements = this.getSelectableElements(relation.getB());
        return elements.map(variable -> variable.get().stream()
                .map(b -> {
                    final var instance = new ElementCombinationTuple<>(this.solutionElementPrototype);
                    instance.setValue(relation.getB(), b);
                    return instance;
                })
                .toList()
        ).orElse(List.of());
    }
}
