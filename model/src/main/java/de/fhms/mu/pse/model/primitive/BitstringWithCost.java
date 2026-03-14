package de.fhms.mu.pse.model.primitive;

import lombok.Data;

@Data
public class BitstringWithCost {
    private final Bitstring bitstring;
    private final Double cost;
}
