package org.navneet.queuectl.cli;

import org.navneet.queuectl.Job;
import org.navneet.queuectl.JobRepository;
import org.navneet.queuectl.JobState;
import picocli.CommandLine.Command;

import java.util.List;

@Command(
        name = "list",
        description = "List dead letter queue jobs"
)
public class DlqListCommand implements Runnable {

    private final JobRepository jobRepository = new JobRepository();

    @Override
    public void run() {
        List<Job> deadJobs = jobRepository.findByState(JobState.DEAD);

        if (deadJobs.isEmpty()) {
            System.out.println("DLQ is empty.");
            return;
        }

        System.out.printf("%-36s %-10s %-30s %-25s%n",
                "ID", "ATTEMPTS", "COMMAND", "UPDATED AT");
        System.out.println("-".repeat(105));

        for (Job job : deadJobs) {
            System.out.printf("%-36s %-10s %-30s %-25s%n",
                    job.getId(),
                    job.getAttempts() + "/" + job.getMaxRetries(),
                    truncate(job.getCommand(), 30),
                    job.getUpdatedAt());
        }
    }

    private String truncate(String s, int maxLen) {
        if (s == null) return "";
        return s.length() > maxLen ? s.substring(0, maxLen - 3) + "..." : s;
    }
}