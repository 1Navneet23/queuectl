package org.navneet.queuectl;


import java.time.Instant;
import java.util.Optional;

public class Worker implements Runnable {

    // Poll database every second if no jobs are available
    private static final long POLL_INTERVAL_MS = 1000;

    private final ConfigRepository configRepo = new ConfigRepository();

    private final int backoffBase = Integer.parseInt(
            configRepo.get("backoff-base", "2")
    );
    private final String workerId;
    private final JobRepository repository;

    private volatile boolean running = true;

    public Worker(String workerId, JobRepository repository) {
        this.workerId = workerId;
        this.repository = repository;
    }

    @Override
    public void run() {

        System.out.println("Worker started: " + workerId);

        while (running) {

            try {

                Optional<Job> optionalJob = repository.claimNextJob(workerId);

                // No job available
                if (optionalJob.isEmpty()) {
                    Thread.sleep(POLL_INTERVAL_MS);
                    continue;
                }

                Job job = optionalJob.get();

                System.out.println("----------------------------------------");
                System.out.println("Worker : " + workerId);
                System.out.println("Job    : " + job.getId());
                System.out.println("Command: " + job.getCommand());

                ExecutionResult result =
                        CommandExecutor.executeCommand(job.getCommand());

                job.setUpdatedAt(Instant.now());

                if (result.isSuccess()) {

                    job.setState(JobState.COMPLETED);
                    job.setNextRunAt(null);
                    System.out.println("Status : COMPLETED");

                } else {

                    job.setAttempts(job.getAttempts() + 1);

                    if (job.getAttempts() >= job.getMaxRetries()) {

                        job.setState(JobState.DEAD);
                        job.setNextRunAt(null);
                        System.out.println("Status : DEAD");
                        System.out.println("Reason : Maximum retries exceeded");

                    } else {

                        job.setState(JobState.PENDING);

                        long delay =
                                (long) Math.pow(backoffBase, job.getAttempts());
                        Instant nextRun =
                                Instant.now().plusSeconds(delay);

                        job.setNextRunAt(nextRun);

                        // Release ownership so another worker can claim later
                        job.setClaimedBy(null);

                        System.out.println("Status : RETRY");
                        System.out.println("Attempts: "
                                + job.getAttempts()
                                + "/"
                                + job.getMaxRetries());

                        System.out.println("Next Run: " + nextRun);
                    }
                }

                repository.update(job);

                System.out.println("Exit Code : " + result.getExitCode());

                if (!result.getOutput().isBlank()) {
                    System.out.println("Output:");
                    System.out.println(result.getOutput());
                }

            } catch (InterruptedException e) {

                Thread.currentThread().interrupt();
                running = false;

            } catch (Exception e) {

                System.err.println("Worker error:");
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