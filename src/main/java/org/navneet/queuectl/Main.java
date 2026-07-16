package org.navneet.queuectl;

public class Main {

    public static void main(String[] args) throws Exception {

        // Create database/table
        Database.createTable();

        JobRepository repository = new JobRepository();

        // Seed jobs
        repository.insert(new Job("echo Hello QueueCTL", 3));
        repository.insert(new Job("dir", 3));
        repository.insert(new Job("asdfasdf", 3));

        // Create worker
        Worker worker = new Worker("worker-1", repository);

        // Run worker on current thread
        worker.run();
    }
}