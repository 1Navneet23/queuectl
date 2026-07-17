package org.navneet.queuectl;

public class Main {

    public static void main(String[] args) throws Exception {

        Database.createTable();
        JobRepository repository = new JobRepository();

        ConfigRepository configRepo = new ConfigRepository();

        int maxRetries = Integer.parseInt(
                configRepo.get("max-retries", "5")
        );
        repository.insert(new Job("echo Job 1", maxRetries));
        repository.insert(new Job("echo Job 2", maxRetries));
        repository.insert(new Job("echo Job 3", maxRetries));
        repository.insert(new Job("echo Job 4", maxRetries));
        repository.insert(new Job("echo Job 5", maxRetries));

        repository.insert(new Job("ping 127.0.0.1 -n 20 > nul", maxRetries));
        repository.insert(new Job("asdfasdf", maxRetries));
        WorkerManager manager =
                new WorkerManager(3, repository);



        Runtime.getRuntime().addShutdownHook(new Thread(() -> {

            System.out.println("\nShutdown signal received.");

            manager.stop();

            System.out.println("Shutdown complete.");

        }));


        manager.start();

        // Keep Main alive
        while (true) {
            Thread.sleep(1000);
        }
    }
}