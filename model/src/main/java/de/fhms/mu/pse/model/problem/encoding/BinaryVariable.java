package de.fhms.mu.pse.model.problem.encoding;

public class BinaryVariable<T> extends EncodingVariable<T, Byte> {
    public BinaryVariable(String name, T data) {
        this(name, data, (byte) 0);
    }

    public BinaryVariable(String name, T data, byte value) {
        super(name, data, value);
    }

    public boolean getBoolean() {
        return this.getValue() != 0;
    }

    @Override
    public void setValue(Byte value) {
        super.setValue((byte) (value != 0 ? 1 : 0));
    }
}
