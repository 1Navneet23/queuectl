package org.navneet.queuectl.cli;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.navneet.queuectl.ConfigRepository;
import org.navneet.queuectl.Job;
import org.navneet.queuectl.JobRepository;
import org.navneet.queuectl.JobState;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.time.Instant;
import java.util.UUID;

@Command(name = "enqueue", description = "Add a new job to the queue")
public class EnqueueCommand implements Runnable {

    @Parameters(index = "0", description = "Job definition as JSON, e.g. '{\"id\":\"job1\",\"command\":\"sleep 2\"}'")
    private String jobJson;

    private final ObjectMapper mapper = new ObjectMapper();
    private final JobRepository jobRepository = new JobRepository();
    private final ConfigRepository configRepository = new ConfigRepository();

    @Override
    public void run() {
        try {
            EnqueueRequest request = mapper.readValue(jobJson, EnqueueRequest.class);

            if (request.command == null || request.command.isBlank()) {
                System.err.println("Error: \"command\" is required.");
                return;
            }

            String id = (request.id == null || request.id.isBlank())
                    ? UUID.randomUUID().toString()
                    : request.id;

            int defaultMaxRetries = Integer.parseInt(
                    configRepository.get("max-retries", "3")
            );
            int maxRetries = (request.maxRetries != null) ? request.maxRetries : defaultMaxRetries;

            Instant now = Instant.now();

            Job job = new Job(id, request.command, JobState.PENDING, 0, maxRetries, now, now);
            job.setNextRunAt(null);
            job.setClaimedBy(null);

            jobRepository.insert(job);

            System.out.println("Job enqueued successfully.");
            System.out.println("ID         : " + job.getId());
            System.out.println("Command    : " + job.getCommand());
            System.out.println("Max Retries: " + job.getMaxRetries());

        } catch (Exception e) {
            System.err.println("Error: Invalid job JSON - " + e.getMessage());
        }
    }

    private static class EnqueueRequest {
        public String id;
        public String command;

        @JsonProperty("max_retries")
        public Integer maxRetries;
    }
}