package de.fhms.mu.pse.model.primitive;

import java.util.Arrays;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Bitstring {
    private final byte[] bits;

    public Bitstring(int length, Random random) {
        this.bits = new byte[length];

        for (int i = 0; i < length; i++) {
            this.bits[i] = (byte) (random.nextBoolean() ? 1 : 0);
        }
    }

    public Bitstring(byte[] bits) {
        this(bits.length);
        this.fill(bits);
    }

    public Bitstring(int length) {
        this.bits = new byte[length];
        this.reset();
    }

    public Bitstring(Bitstring other) {
        this.bits = other.bits.clone();
    }

    public int length() {
        return this.bits.length;
    }

    public long getNumericValue() {
        long value = 0L;
        for (final var bit : this.bits) {
            value = (value << 1) | bit;
        }
        return value;
    }

    public byte get(int index) {
        return this.bits[index];
    }

    public boolean getAsBoolean(int index) {
        return this.bits[index] > 0;
    }

    public Bitstring reset() {
        Arrays.fill(this.bits, (byte) 0);
        return this;
    }

    public Bitstring set(int index, byte value) {
        this.bits[index] = value;
        return this;
    }

    public Bitstring fill(long value) {
        // this.reset();
        for (int i = 0; i < this.bits.length; i++) {
            this.bits[this.bits.length - 1 - i] = (byte) ((value >> i) & 1);
        }
        return this;
    }

    public Bitstring fill(Bitstring other) {
        return this.fill(other.bits);
    }

    public Bitstring fill(byte[] values) {
        // this.reset();
        System.arraycopy(values, 0, this.bits, 0, this.length());
        return this;
    }

    public Bitstring flip() {
        for (int i = 0; i < this.bits.length; i++) {
            this.flip(i);
        }
        return this;
    }

    public Bitstring flip(int index) {
        this.bits[index] = (byte) (this.getAsBoolean(index) ? 0 : 1);
        return this;
    }

    public Bitstring sub(int length) {
        return new Bitstring(length).fill(this.bits);
    }

    public String toString(boolean compact) {
        if (!compact) {
            return this.toString();
        }

        return IntStream.range(0, this.bits.length)
                .mapToObj(i -> String.valueOf(this.bits[i]))
                .collect(Collectors.joining());
    }

    @Override
    public String toString() {
        return Arrays.toString(this.bits);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Bitstring bitstring = (Bitstring) o;
        return Objects.deepEquals(this.bits, bitstring.bits);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getNumericValue());
    }
}
