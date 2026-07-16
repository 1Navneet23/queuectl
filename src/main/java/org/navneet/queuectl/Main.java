package org.navneet.queuectl;

public class Main {

    public static void main(String[] args) {

        Database.createTable();

        JobRepository repository = new JobRepository();

        // Seed jobs
        repository.insert(new Job("echo Job 1", 3));
        repository.insert(new Job("echo Job 2", 3));
        repository.insert(new Job("echo Job 3", 3));
        repository.insert(new Job("echo Job 4", 3));
        repository.insert(new Job("echo Job 5", 3));
        repository.insert(new Job("dir", 3));
        repository.insert(new Job("asdfasdf", 3));

        WorkerManager manager =
                new WorkerManager(3, repository);

        manager.start();

        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        manager.stop();
    }
}