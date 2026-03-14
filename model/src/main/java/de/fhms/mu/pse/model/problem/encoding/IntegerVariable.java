package de.fhms.mu.pse.model.problem.encoding;

import lombok.Getter;

import java.util.List;
import java.util.stream.IntStream;

@Getter
public class IntegerVariable extends EncodingVariable<Integer, Integer> {
    private final List<BinaryVariable<Integer>> binaryVariables;

    public IntegerVariable(String name, Integer maxValue) {
        super(name, maxValue);

        this.binaryVariables = createBinaryVariables(name, maxValue);
    }

    @Override
    public void setValue(Integer value) {
        super.setValue(value);

        this.fillBinaryVariables(value);
    }

    @Override
    public Integer getValue() {
        return IntStream.range(0, this.binaryVariables.size())
                .map(i -> (int) Math.pow(2, i) * this.binaryVariables.get(i).getValue())
                .sum();
    }

    public static List<BinaryVariable<Integer>> createBinaryVariables(String prefix, int maxValue) {
        final var n = (int) (Math.floor(Math.log(maxValue) / Math.log(2))) + 1;
        return IntStream.range(0, n)
                .mapToObj(i -> new BinaryVariable<>(prefix + "_" + i, (int) Math.pow(2, i)))
                .toList();
    }

    private void fillBinaryVariables(int value) {
        final var n = this.binaryVariables.size();
        for (int i = 0; i < n; i++) {
            this.binaryVariables.get(n - 1 - i).setValue((byte) ((value >> i) & 1));
        }
    }
}
