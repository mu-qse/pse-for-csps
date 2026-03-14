package de.fhms.mu.pse.model.solver;

import java.time.Duration;

public class SolverUtils {
    public static String formatDeltaTime(Duration delta) {
        final var HH = delta.getSeconds() / 3600;
        final var MM = (delta.getSeconds() % 3600) / 60;
        final var SS = delta.getSeconds() % 60;
        final var ms = delta.getNano() / 1000000;
        return String.format("%01d:%02d:%02d.%03d", HH, MM, SS, ms);
    }
}
