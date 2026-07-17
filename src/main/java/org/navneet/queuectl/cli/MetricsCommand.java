package org.navneet.queuectl.cli;

import org.navneet.queuectl.Job;
import org.navneet.queuectl.JobRepository;
import org.navneet.queuectl.JobState;
import picocli.CommandLine.Command;

import java.util.List;

@Command(name = "metrics", description = "Show execution statistics")
public class MetricsCommand implements Runnable {

    private final JobRepository jobRepository = new JobRepository();

    @Override
    public void run() {
        List<Job> jobs = jobRepository.findAll();

        if (jobs.isEmpty()) {
            System.out.println("No jobs found.");
            return;
        }

        long completed = jobs.stream().filter(j -> j.getState() == JobState.COMPLETED).count();
        long dead = jobs.stream().filter(j -> j.getState() == JobState.DEAD).count();
        long timedOut = jobs.stream().filter(Job::isTimedOut).count();

        double avgAttempts = jobs.stream().mapToInt(Job::getAttempts).average().orElse(0);

        double avgDurationMs = jobs.stream()
                .filter(j -> j.getDurationMs() != null)
                .mapToLong(Job::getDurationMs)
                .average()
                .orElse(0);

        double successRate = jobs.isEmpty() ? 0 : (completed * 100.0 / jobs.size());

        System.out.println("==================================");
        System.out.println("QueueCTL Metrics");
        System.out.println("==================================");
        System.out.printf("Total Jobs        : %d%n", jobs.size());
        System.out.printf("Completed         : %d%n", completed);
        System.out.printf("Dead (DLQ)        : %d%n", dead);
        System.out.printf("Timed Out (ever)  : %d%n", timedOut);
        System.out.printf("Success Rate      : %.1f%%%n", successRate);
        System.out.printf("Avg Attempts/Job  : %.2f%n", avgAttempts);
        System.out.printf("Avg Duration      : %.0fms%n", avgDurationMs);
    }
}