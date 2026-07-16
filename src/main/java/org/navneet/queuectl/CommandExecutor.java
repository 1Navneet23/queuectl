package org.navneet.queuectl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CommandExecutor {

    public static ExecutionResult executeCommand(String command) {

        ProcessBuilder builder = new ProcessBuilder("cmd", "/c", command);

        // Merge stdout and stderr
        builder.redirectErrorStream(true);

        try {
            Process process = builder.start();

            StringBuilder output = new StringBuilder();

            try (BufferedReader reader =
                         new BufferedReader(
                                 new InputStreamReader(process.getInputStream()))) {

                String line;

                while ((line = reader.readLine()) != null) {
                    output.append(line)
                            .append(System.lineSeparator());
                }
            }

            int exitCode = process.waitFor();

            return new ExecutionResult(exitCode, output.toString());

        } catch (IOException | InterruptedException e) {

            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }

            return new ExecutionResult(-1, e.getMessage());
        }
    }
}