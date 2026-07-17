package org.navneet.queuectl.cli;

import org.navneet.queuectl.Job;
import org.navneet.queuectl.JobRepository;
import org.navneet.queuectl.JobState;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.List;

@Command(
        name = "list",
        description = "List jobs in the queue"
)
public class ListCommand implements Runnable {

    @Option(names = "--state", description = "Filter by job state: pending, processing, completed, failed, dead")
    private String state;

    private final JobRepository jobRepository = new JobRepository();

    @Override
    public void run() {
        List<Job> jobs;

        if (state != null) {
            JobState jobState;
            try {
                jobState = JobState.valueOf(state.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                System.err.println("Error: invalid state \"" + state + "\". Valid values: pending, processing, completed, failed, dead");
                return;
            }
            jobs = jobRepository.findByState(jobState);
        } else {
            jobs = jobRepository.findAll();
        }

        if (jobs.isEmpty()) {
            System.out.println("No jobs found.");
            return;
        }

        System.out.printf("%-36s %-10s %-10s %-25s %-20s%n",
                "ID", "STATE", "ATTEMPTS", "COMMAND", "NEXT RUN");
        System.out.println("-".repeat(105));

        for (Job job : jobs) {
            System.out.printf("%-36s %-10s %-10s %-25s %-20s%n",
                    job.getId(),
                    job.getState(),
                    job.getAttempts() + "/" + job.getMaxRetries(),
                    truncate(job.getCommand(), 25),
                    job.getNextRunAt() != null ? job.getNextRunAt() : "-");
        }
    }

    private String truncate(String s, int maxLen) {
        if (s == null) return "";
        return s.length() > maxLen ? s.substring(0, maxLen - 3) + "..." : s;
    }
}