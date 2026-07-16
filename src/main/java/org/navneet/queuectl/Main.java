package org.navneet.queuectl;

import java.util.List;
import java.util.Optional;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        Database.createTable();
        JobRepository repo = new JobRepository();

        Job job = new Job("echo hello", 3);
        repo.insert(job);
        System.out.println("Inserted: " + job.getId());

        Optional<Job> found = repo.findById(job.getId());
        System.out.println("Found: " + found.isPresent());

        job.setState(JobState.COMPLETED);
        repo.update(job);

        List<Job> completed = repo.findByState(JobState.COMPLETED);
        System.out.println("Completed jobs: " + completed.size());
    }
}