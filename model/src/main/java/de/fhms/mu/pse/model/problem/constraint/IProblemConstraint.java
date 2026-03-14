package de.fhms.mu.pse.model.problem.constraint;

import de.fhms.mu.pse.model.problem.IProblem;

public interface IProblemConstraint<P extends IProblem<S>, S> {
    String getName();
    double getWeight();
    void setWeight(double weight);
}
