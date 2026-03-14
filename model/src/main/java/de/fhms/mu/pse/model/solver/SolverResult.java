package de.fhms.mu.pse.model.solver;

import de.fhms.mu.pse.model.primitive.Bitstring;
import de.fhms.mu.pse.model.primitive.BitstringWithCost;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Data
public class SolverResult {
    private static final Double EPSILON = 0.0000001;

    private final List<Bitstring> bitstrings;
    private final Map<Integer, Bitstring> bitstringsMap;
    private final List<Double> costs;
    private final List<Integer> solutionIndices = new ArrayList<>();
    private double lowestCost = Double.MAX_VALUE;

    public SolverResult() {
        this(0);
    }

    public SolverResult(long count) {
        this.bitstrings = new ArrayList<>((int) count);
        this.bitstringsMap =  new HashMap<>((int) count);
        this.costs = new ArrayList<>((int) count);
    }

    public SolverResult add(SolverResult other) {
        other.getBitstrings().forEachOrdered(this::add);
        return this;
    }

    public void add(BitstringWithCost bitstringWithCost) {
        this.add(bitstringWithCost.getBitstring(), bitstringWithCost.getCost());
    }

    public void add(Bitstring bitstring, Double cost) {
        final var key = bitstring.hashCode();
        if (this.bitstringsMap.containsKey(key)) {
            return;
        }

        final var copy = new Bitstring(bitstring);
        this.bitstrings.add(copy);
        this.bitstringsMap.put(key, copy);
        this.costs.add(cost);

        final var solutionIndex = this.bitstrings.size() - 1;
        if (cost < this.lowestCost) {
            this.solutionIndices.clear();
            this.solutionIndices.add(solutionIndex);
            this.lowestCost = cost;
        } else if (Math.abs(cost - this.lowestCost) < EPSILON) {
            this.solutionIndices.add(solutionIndex);
        }
    }

    public int getSolutionBitstringsCount() {
        return this.solutionIndices.size();
    }

    public Stream<BitstringWithCost> getSolutionBitstrings() {
        return this.solutionIndices.stream()
                .map(this::getBitstring);
    }

    public BitstringWithCost getSolutionBitstring(int index) {
        final var solutionIndex = this.solutionIndices.get(index);
        return this.getBitstring(solutionIndex);
    }

    public Stream<BitstringWithCost> getBitstrings() {
        return IntStream.range(0, this.bitstrings.size())
                .mapToObj(this::getBitstring);
    }

    public BitstringWithCost getBitstring(int index) {
        return new BitstringWithCost(this.bitstrings.get(index), this.costs.get(index));
    }
}
