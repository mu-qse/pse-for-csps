package de.fhms.mu.pse.model.qubo;

public interface IQuboProblem<
        TEncoding extends IQuboProblemEncoding<T>,
        T
> {
    TEncoding getEncoding();
    QuboMatrix buildQuboMatrix();
    double getOffsetValue();
}
