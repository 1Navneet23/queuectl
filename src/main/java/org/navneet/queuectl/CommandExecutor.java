package org.navneet.queuectl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

public class CommandExecutor {

    private static final Path LOG_DIR = Path.of("logs");

    private static boolean isWindows() {
        String os = System.getProperty("os.name");
        return os != null && os.toLowerCase().contains("win");
    }

    /** Backward-compatible overload: no timeout enforced. */
    public static ExecutionResult executeCommand(String command) {
        return executeCommand(command, "unknown-job", 0, 0);
    }

    /**
     * @param timeoutSeconds 0 or negative = no timeout
     */
    public static ExecutionResult executeCommand(String command, String jobId,
                                                 int attemptNumber, int timeoutSeconds) {

        ProcessBuilder builder = isWindows()
                ? new ProcessBuilder("cmd", "/c", command)
                : new ProcessBuilder("sh", "-c", command);

        builder.redirectErrorStream(true);

        String logPath = null;
        long start = System.currentTimeMillis();

        try {
            Files.createDirectories(LOG_DIR);
            logPath = LOG_DIR.resolve(jobId + "_attempt" + attemptNumber + ".log").toString();
        } catch (IOException e) {
            System.err.println("Warning: could not create logs directory: " + e.getMessage());
        }

        Process process = null;
        StringBuilder output = new StringBuilder();
        final String finalLogPath = logPath;
        Thread readerThread = null;

        try {
            process = builder.start();
            Process runningProcess = process;

            // Drain stdout/stderr on a separate thread so it never blocks
            // the timeout check below. A hung/long-running process would
            // otherwise stall readLine() until it exits on its own,
            // defeating the timeout entirely.
            readerThread = new Thread(() -> {
                PrintWriter logWriter = null;
                try {
                    if (finalLogPath != null) {
                        logWriter = new PrintWriter(Files.newBufferedWriter(Path.of(finalLogPath)));
                    }
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(runningProcess.getInputStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            output.append(line).append(System.lineSeparator());
                            if (logWriter != null) {
                                logWriter.println(line);
                            }
                        }
                    }
                } catch (IOException ignored) {
                    // stream closed because process was killed — expected on timeout
                } finally {
                    if (logWriter != null) {
                        logWriter.close();
                    }
                }
            });
            readerThread.setDaemon(true);
            readerThread.start();

            boolean finished;
            if (timeoutSeconds > 0) {
                finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            } else {
                process.waitFor();
                finished = true;
            }

            if (!finished) {
                process.destroyForcibly();
                process.waitFor(5, TimeUnit.SECONDS); // grace period for OS cleanup
                readerThread.join(2000); // let the reader flush what it captured, bounded wait
                long duration = System.currentTimeMillis() - start;
                ExecutionResult result = new ExecutionResult(
                        -1, output + "\n[TIMED OUT after " + timeoutSeconds + "s]", true, duration);
                result.setLogPath(logPath);
                return result;
            }

            // Process exited normally — wait for the reader thread to finish
            // flushing the last of the output (join establishes happens-before,
            // so reading `output` afterward is safe without extra locking).
            readerThread.join(5000);

            int exitCode = process.exitValue();
            long duration = System.currentTimeMillis() - start;
            ExecutionResult result = new ExecutionResult(exitCode, output.toString(), false, duration);
            result.setLogPath(logPath);
            return result;

        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            if (process != null) {
                process.destroyForcibly();
            }
            long duration = System.currentTimeMillis() - start;
            return new ExecutionResult(-1, e.getMessage(), false, duration);
        }
    }
}