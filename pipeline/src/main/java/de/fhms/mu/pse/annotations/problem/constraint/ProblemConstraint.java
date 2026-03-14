package de.fhms.mu.pse.annotations.problem.constraint;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ProblemConstraint {
    String name() default "";
    double weight() default 0.0;
}
