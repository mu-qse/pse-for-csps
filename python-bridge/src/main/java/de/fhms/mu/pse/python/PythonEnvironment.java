package de.fhms.mu.pse.python;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Getter
public class PythonEnvironment {
    private final File environmentPath;
    private final File requirementsPath;

    public PythonEnvironment(File workingPath) {
        this.environmentPath = new File(workingPath, ".venv");
        this.requirementsPath =  new File(workingPath, "requirements.txt");
    }

    public void setup() throws IOException {
        System.out.printf("Setting up python environment in: %s ...%n", this.environmentPath);

        final var globalPythonVersion = this.getGlobalPythonVersion();
        System.out.println("Global python version: " + globalPythonVersion);

        final var environmentPythonPath = this.getEnvironmentPythonPath();
        if (!environmentPythonPath.exists()) {
            this.createVirtualEnvironment(this.environmentPath);
        }

        final var environmentPythonVersion = this.getEnvironmentPythonVersion();
        System.out.println("Environment python version: " + environmentPythonVersion);

        System.out.printf("Installing packages from: %s ...%n", this.requirementsPath);
        this.upgradePip();
        this.installPackages(this.requirementsPath);
    }

    public String getGlobalPythonVersion() throws IOException {
        final var pythonPath = this.getGlobalPythonExecutable();
        final var invocation = this.invokePython(pythonPath, false, "--version");
        if (invocation.hasFailed()) {
            throw new IllegalStateException("Could not get python version");
        }
        return invocation.getFullOutput().replace("Python", "").trim();
    }

    public String getEnvironmentPythonVersion() throws IOException {
        final var pythonPath = this.getEnvironmentPythonPath();
        final var invocation = this.invokePython(pythonPath, false, "--version");
        if (invocation.hasFailed()) {
            throw new IllegalStateException("Could not get python version of virtual environment");
        }
        return invocation.getFullOutput().replace("Python", "").trim();
    }

    public Invocation invokeScript(File scriptPath, String... arguments) throws IOException {
        final var pythonPath = this.getEnvironmentPythonPath();
        if (!pythonPath.exists()) {
            throw new IllegalStateException("Virtual environment is not set up yet");
        }

        final var executable = Stream.concat(
                Stream.of(pythonPath.getAbsolutePath(), scriptPath.getAbsolutePath()),
                Arrays.stream(arguments)
        ).toList();
        return this.invoke(executable, true);
    }

    private void createVirtualEnvironment(File environmentPath) throws IOException {
        final var pythonPath = this.getGlobalPythonExecutable();
        final var invocation = this.invokePython(pythonPath, false, "-m", "venv", environmentPath.getAbsolutePath());
        if (invocation.hasFailed()) {
            throw new IllegalStateException("Could not create virtual environment");
        }
    }

    private void upgradePip() throws IOException {
        final var pythonPath = this.getEnvironmentPythonPath();
        final var invocation = this.invokePython(pythonPath, false, "-m", "pip", "install", "--upgrade", "pip");
        if (invocation.hasFailed()) {
            throw new IllegalStateException("Could not upgrade pip in virtual environment");
        }
    }

    private void installPackages(File requirementsPath) throws IOException {
        final var pipPath = this.getEnvironmentPipPath();
        final var invocation = this.invokePip(pipPath, false, "install", "-r", requirementsPath.getAbsolutePath());
        if (invocation.hasFailed()) {
            throw new IllegalStateException("Could not install packages in virtual environment");
        }
    }

    private Invocation invokePython(File pythonPath, boolean printOutput, String... arguments) throws IOException {
        return this.invokePython(pythonPath.getAbsolutePath(), printOutput, arguments);
    }

    private Invocation invokePython(String pythonPath, boolean printOutput, String... arguments) throws IOException {
        final var command = Stream.concat(
                Stream.of(pythonPath),
                Arrays.stream(arguments)
        ).toList();
        return this.invoke(command, printOutput);
    }

    private Invocation invokePip(File pipPath, boolean printOutput, String... arguments) throws IOException {
        return this.invokePip(pipPath.getAbsolutePath(), printOutput, arguments);
    }

    private Invocation invokePip(String pipPath, boolean printOutput, String... arguments) throws IOException {
        final var command = Stream.concat(
                Stream.of(pipPath),
                Arrays.stream(arguments)
        ).toList();
        return this.invoke(command, printOutput);
    }

    private Invocation invoke(List<String> command, boolean printOutput) throws IOException {
        final var process = new ProcessBuilder(command.toArray(String[]::new))
                .redirectErrorStream(true)
                .start();

        final var output = new ArrayList<String>();
        final var readOutputThread = new Thread(() -> {
            try (final var input = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = input.readLine()) != null) {
                    output.add(line);
                    if (printOutput) {
                        System.out.println(line);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        /*final var error = new ArrayList<String>();
        final var readErrorThread = new Thread(() -> {
            try (final var input = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = input.readLine()) != null) {
                    error.add(line);
                    System.err.println(line);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });*/

        readOutputThread.start();
        //readErrorThread.start();

        try {
            readOutputThread.join();
            //readErrorThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return new Invocation(
                command,
                process.exitValue(),
                output
                //error
        );
    }

    private String getGlobalPythonExecutable() {
        return this.isWindows() ? "python.exe" : "python3";
    }

    private File getEnvironmentPythonPath() {
        return new File(this.environmentPath, this.isWindows() ? "Scripts\\python.exe" : "bin/python3");
    }

    private File getEnvironmentPipPath() {
        return new File(this.environmentPath, this.isWindows() ? "Scripts\\pip.exe" : "bin/pip");
    }

    private boolean isWindows() {
        return System.getProperty("os.name").contains("Windows");
    }

    @Data
    public static class Invocation {
        private final List<String> command;
        private final int exitCode;
        private final List<String> output;
        //private final List<String> error;

        public boolean hasFailed() {
            return this.exitCode != 0;
        }

        public String getFullOutput() {
            return String.join("\n", this.output);
        }
    }
}
