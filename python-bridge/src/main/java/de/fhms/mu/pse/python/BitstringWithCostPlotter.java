package de.fhms.mu.pse.python;

import de.fhms.mu.pse.model.primitive.BitstringWithCost;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class BitstringWithCostPlotter {
    private final PythonEnvironment pythonEnvironment;

    public PythonEnvironment.Invocation plot(String title, File outputPath, Stream<BitstringWithCost> bitstrings) throws IOException {
        outputPath.mkdirs();

        final var csvFile = new File(outputPath, "bitstrings.csv");
        this.exportCsv(csvFile, bitstrings);

        this.pythonEnvironment.setup();
        final var scriptPath = new File(this.pythonEnvironment.getEnvironmentPath(), "../plot_bitstring_cost.py");
        final var plotFile = new File(outputPath, "costs.svg");
        return this.pythonEnvironment.invokeScript(scriptPath, csvFile.getAbsolutePath(), plotFile.getAbsolutePath(), title);
    }

    private void exportCsv(File csvFile, Stream<BitstringWithCost> bitstrings) throws IOException {
        final var header = Stream.of(String.join(",", "numeric", "bitstring", "cost"));
        final var data = bitstrings.map(bitstringWithCost -> {
            final var bitstring = bitstringWithCost.getBitstring();
            return Stream.of(bitstring.getNumericValue(), bitstring.toString(true), bitstringWithCost.getCost())
                    .map(Object::toString)
                    .collect(Collectors.joining(","));
        });
        try (final var fileWriter = new FileWriter(csvFile)) {
            fileWriter.write(Stream.concat(header, data).collect(Collectors.joining("\n")));
        }
    }
}
