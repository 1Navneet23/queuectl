package org.navneet.queuectl.cli;

import org.navneet.queuectl.Job;
import org.navneet.queuectl.JobRepository;
import org.navneet.queuectl.JobState;
import picocli.CommandLine.Command;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Command(name = "status", description = "Show summary of all job states & active workers")
public class StatusCommand implements Runnable {

    private final JobRepository jobRepository = new JobRepository();

    @Override
    public void run() {
        List<Job> jobs = jobRepository.findAll();

        Map<JobState, Integer> counts = new EnumMap<>(JobState.class);
        for (JobState state : JobState.values()) {
            counts.put(state, 0);
        }

        Set<String> activeWorkerIds = new HashSet<>();

        for (Job job : jobs) {
            counts.merge(job.getState(), 1, Integer::sum);
            if (job.getState() == JobState.PROCESSING && job.getClaimedBy() != null) {
                activeWorkerIds.add(job.getClaimedBy());
            }
        }

        System.out.println("==================================");
        System.out.println("QueueCTL Status");
        System.out.println("==================================");
        System.out.println("Total Jobs         : " + jobs.size());
        System.out.println();
        System.out.println("By State:");
        for (JobState state : JobState.values()) {
            System.out.printf("  %-12s: %d%n", state, counts.get(state));
        }
        System.out.println();
        System.out.println("Active Workers*    : " + activeWorkerIds.size()
                + (activeWorkerIds.isEmpty() ? "" : " " + activeWorkerIds));
        System.out.println();
        System.out.println("* inferred from jobs currently PROCESSING; a worker that is");
        System.out.println("  idle/polling won't show here.");
    }
}