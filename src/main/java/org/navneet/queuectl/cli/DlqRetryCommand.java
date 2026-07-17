package org.navneet.queuectl.cli;

import org.navneet.queuectl.Job;
import org.navneet.queuectl.JobRepository;
import org.navneet.queuectl.JobState;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.time.Instant;
import java.util.Optional;

@Command(
        name = "retry",
        description = "Retry a dead letter queue job"
)
public class DlqRetryCommand implements Runnable {

    @Parameters(index = "0", description = "ID of the job to retry")
    private String jobId;

    private final JobRepository jobRepository = new JobRepository();

    @Override
    public void run() {
        Optional<Job> optionalJob = jobRepository.findById(jobId);

        if (optionalJob.isEmpty()) {
            System.err.println("Error: no job found with id \"" + jobId + "\".");
            return;
        }

        Job job = optionalJob.get();

        if (job.getState() != JobState.DEAD) {
            System.err.println("Error: job \"" + jobId + "\" is not in the DLQ (current state: " + job.getState() + ").");
            return;
        }

        job.setState(JobState.PENDING);
        job.setAttempts(0);
        job.setNextRunAt(null);
        job.setClaimedBy(null);
        job.setUpdatedAt(Instant.now());

        jobRepository.update(job);

        System.out.println("Job \"" + jobId + "\" moved back to PENDING and attempts reset to 0.");
    }
}