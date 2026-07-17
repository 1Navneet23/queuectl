package org.navneet.queuectl;

import java.time.Instant;
import java.util.UUID;

public class Job {

    private String id;
    private String command;
    private JobState state;
    private int attempts;
    private int maxRetries;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant nextRunAt;
    private String claimedBy;
    private String logPath;
    private boolean timedOut;
    private Long durationMs;

    public String getLogPath() { return logPath; }
    public void setLogPath(String logPath) { this.logPath = logPath; }

    public boolean isTimedOut() { return timedOut; }
    public void setTimedOut(boolean timedOut) { this.timedOut = timedOut; }

    public Long getDurationMs() { return durationMs; }
    public void setDurationMs(Long durationMs) { this.durationMs = durationMs; }
    public Job() {
        // used when reconstructing a Job from the database
    }

    public Job(String command, int maxRetries) {
        this.id = UUID.randomUUID().toString();
        this.command = command;
        this.state = JobState.PENDING;
        this.attempts = 0;
        this.maxRetries = maxRetries;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        this.nextRunAt = null;
        this.claimedBy = null;
    }

    public Job(String id, String command, JobState state, int attempts, int maxRetries,
               Instant createdAt, Instant updatedAt) {
        this.id = (id == null || id.isBlank()) ? UUID.randomUUID().toString() : id;
        this.command = command;
        this.state = state;
        this.attempts = attempts;
        this.maxRetries = maxRetries;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCommand() { return command; }
    public void setCommand(String command) { this.command = command; }

    public JobState getState() { return state; }
    public void setState(JobState state) { this.state = state; }

    public int getAttempts() { return attempts; }
    public void setAttempts(int attempts) { this.attempts = attempts; }

    public int getMaxRetries() { return maxRetries; }
    public void setMaxRetries(int maxRetries) { this.maxRetries = maxRetries; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public Instant getNextRunAt() { return nextRunAt; }
    public void setNextRunAt(Instant nextRunAt) { this.nextRunAt = nextRunAt; }

    public String getClaimedBy() { return claimedBy; }
    public void setClaimedBy(String claimedBy) { this.claimedBy = claimedBy; }
}