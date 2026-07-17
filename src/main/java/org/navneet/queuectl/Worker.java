package org.navneet.queuectl;

import java.time.Instant;
import java.util.Optional;

public class Worker implements Runnable {

    private static final long POLL_INTERVAL_MS = 1000;

    private final String workerId;
    private final JobRepository repository;
    private final ConfigRepository configRepository;

    private volatile boolean running = true;

    public Worker(String workerId, JobRepository repository) {
        this.workerId = workerId;
        this.repository = repository;
        this.configRepository = new ConfigRepository();
    }

    @Override
    public void run() {

        System.out.println("Worker started: " + workerId);

        // Recover any jobs this worker left in PROCESSING from a previous crash
        repository.releaseStaleJobs(workerId);

        while (running) {

            try {

                Optional<Job> optionalJob = repository.claimNextJob(workerId);

                if (optionalJob.isEmpty()) {
                    Thread.sleep(POLL_INTERVAL_MS);
                    continue;
                }

                Job job = optionalJob.get();

                System.out.println("----------------------------------------");
                System.out.println("Worker : " + workerId);
                System.out.println("Job    : " + job.getId());
                System.out.println("Command: " + job.getCommand());

                ExecutionResult result = CommandExecutor.executeCommand(job.getCommand());

                job.setUpdatedAt(Instant.now());

                if (result.isSuccess()) {

                    job.setState(JobState.COMPLETED);
                    job.setNextRunAt(null);
                    job.setClaimedBy(null);
                    System.out.println("Status : COMPLETED");

                } else {

                    job.setAttempts(job.getAttempts() + 1);

                    if (job.getAttempts() >= job.getMaxRetries()) {

                        job.setState(JobState.DEAD);
                        job.setNextRunAt(null);
                        job.setClaimedBy(null);
                        System.out.println("Status : DEAD");
                        System.out.println("Reason : Maximum retries exceeded");

                    } else {

                        // Read backoff-base fresh each time so config changes take effect
                        int backoffBase = Integer.parseInt(
                                configRepository.get("backoff-base", "2")
                        );

                        long delay = (long) Math.pow(backoffBase, job.getAttempts());
                        Instant nextRun = Instant.now().plusSeconds(delay);

                        // Correct lifecycle: PROCESSING -> FAILED (retryable) with next_run_at set
                        job.setState(JobState.FAILED);
                        job.setNextRunAt(nextRun);
                        job.setClaimedBy(null);

                        System.out.println("Status  : FAILED (will retry)");
                        System.out.println("Attempts: " + job.getAttempts() + "/" + job.getMaxRetries());
                        System.out.println("Next Run: " + nextRun);
                    }
                }

                repository.update(job);

                System.out.println("Exit Code : " + result.getExitCode());

                if (result.getOutput() != null && !result.getOutput().isBlank()) {
                    System.out.println("Output:");
                    System.out.println(result.getOutput());
                }

            } catch (InterruptedException e) {

                Thread.currentThread().interrupt();
                running = false;

            } catch (Exception e) {

                System.err.println("Worker error: " + e.getMessage());
                e.printStackTrace();
            }
        }

        System.out.println("Worker stopped: " + workerId);
    }

    public void stop() {
        running = false;
    }

    public boolean isRunning() {
        return running;
    }

    public String getWorkerId() {
        return workerId;
    }
}