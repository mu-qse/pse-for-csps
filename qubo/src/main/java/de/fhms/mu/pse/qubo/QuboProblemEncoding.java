package de.fhms.mu.pse.qubo;

import de.fhms.mu.pse.model.primitive.Bitstring;
import de.fhms.mu.pse.model.primitive.Pair;
import de.fhms.mu.pse.model.primitive.matrix.MatrixIndex;
import de.fhms.mu.pse.model.problem.encoding.BinaryVariable;
import de.fhms.mu.pse.model.problem.encoding.EncodingVariable;
import de.fhms.mu.pse.model.problem.encoding.ProblemEncoding;
import de.fhms.mu.pse.model.qubo.IQuboProblemEncoding;
import de.fhms.mu.pse.model.qubo.IQuboSolution;
import de.fhms.mu.pse.model.qubo.QuboSolution;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Getter
public class QuboProblemEncoding<
        T
> extends ProblemEncoding<
        BinaryVariable<T>
> implements IQuboProblemEncoding<
        T
> {
    public QuboProblemEncoding() {
        this(new ArrayList<>());
    }

    protected QuboProblemEncoding(List<BinaryVariable<T>> variables) {
        super();

        variables.forEach(this::add);
    }

    @Override
    public Pair<BinaryVariable<T>> getVariablePair(MatrixIndex index) {
        final var variables = this.getVariables();
        return new Pair<>(variables.get(index.row()), variables.get(index.col()));
    }

    @Override
    public Stream<BinaryVariable<T>> getActivatedVariables(Bitstring bitstring) {
        final var variables = this.getVariables();
        return IntStream.range(0, variables.size())
                .filter(bitstring::getAsBoolean)
                .mapToObj(variables::get);
    }

    @Override
    public Stream<IQuboSolution<T>> getSolutions(List<Bitstring> bitstrings) {
        return bitstrings.stream()
                .map(this::mapBitstringToSolution);
    }

    @Override
    public QuboSolution<T> mapBitstringToSolution(Bitstring bitstring) {
        final var values = this.getActivatedVariables(bitstring)
                .map(EncodingVariable::getData)
                .toList();
        return new QuboSolution<>(bitstring, values);
    }
}
