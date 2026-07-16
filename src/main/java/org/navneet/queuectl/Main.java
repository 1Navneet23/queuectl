package org.navneet.queuectl;

import java.util.List;
import java.util.Optional;

public class Main {
    public static void main(String[] args) {
        Database.createTable();
        JobRepository repo = new JobRepository();

        // 1. Insert a few jobs
        Job job1 = new Job("echo hello-1", 3);
        Job job2 = new Job("echo hello-2", 3);
        Job job3 = new Job("echo hello-3", 3);
        repo.insert(job1);
        repo.insert(job2);
        repo.insert(job3);
        System.out.println("Inserted 3 jobs: " + job1.getId() + ", " + job2.getId() + ", " + job3.getId());

        // 2. Check they're all pending
        List<Job> pending = repo.findByState(JobState.PENDING);
        System.out.println("Pending count before claiming: " + pending.size());

        // 3. Claim from two different workers, back to back
        Optional<Job> claimedByWorker1 = repo.claimNextJob("worker-1");
        Optional<Job> claimedByWorker2 = repo.claimNextJob("worker-2");

        System.out.println("worker-1 claimed: " +
                claimedByWorker1.map(Job::getId).orElse("NOTHING"));
        System.out.println("worker-2 claimed: " +
                claimedByWorker2.map(Job::getId).orElse("NOTHING"));

        // TODO: add an assertion-style check here —
        // if both claimed jobs exist, are their IDs actually different?
        // print something clear like "PASS: different jobs claimed" or "FAIL: duplicate claim!"

        // 4. Claim a 3rd time — should get job3
        Optional<Job> claimedByWorker3 = repo.claimNextJob("worker-3");
        System.out.println("worker-3 claimed: " +
                claimedByWorker3.map(Job::getId).orElse("NOTHING"));

        // 5. Claim a 4th time — queue should be empty now
        Optional<Job> claimedByWorker4 = repo.claimNextJob("worker-4");
        System.out.println("worker-4 claimed: " +
                claimedByWorker4.map(Job::getId).orElse("NOTHING (expected — queue empty)"));

        // 6. Check states directly from DB
        List<Job> stillPending = repo.findByState(JobState.PENDING);
        List<Job> processing = repo.findByState(JobState.PROCESSING);
        System.out.println("Still pending: " + stillPending.size());
        System.out.println("Now processing: " + processing.size());
    }
}