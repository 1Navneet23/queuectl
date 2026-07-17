package org.navneet.queuectl;

public class ExecutionResult {

    private final int exitCode;
    private final String output;
    private final boolean timedOut;
    private final long durationMs;
    private String logPath;

    /** Backward-compatible constructor. */
    public ExecutionResult(int exitCode, String output) {
        this(exitCode, output, false, 0);
    }

    public ExecutionResult(int exitCode, String output, boolean timedOut, long durationMs) {
        this.exitCode = exitCode;
        this.output = output;
        this.timedOut = timedOut;
        this.durationMs = durationMs;
    }

    public int getExitCode() {
        return exitCode;
    }

    public String getOutput() {
        return output;
    }

    public boolean isTimedOut() {
        return timedOut;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public String getLogPath() {
        return logPath;
    }

    public void setLogPath(String logPath) {
        this.logPath = logPath;
    }

    public boolean isSuccess() {
        return !timedOut && exitCode == 0;
    }
}