package org.navneet.queuectl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class WorkerManager {

    private final ExecutorService executor;
    private final List<Worker> workers;

    public WorkerManager(int workerCount, JobRepository repository) {

        executor = Executors.newFixedThreadPool(workerCount);
        workers = new ArrayList<>();

        ConfigRepository configRepo = new ConfigRepository();

        int backoffBase = Integer.parseInt(
                configRepo.get("backoff-base", "2")
        );

        for (int i = 1; i <= workerCount; i++) {
            workers.add(new Worker("worker-" + i, repository, backoffBase));
        }
    }

    public void start() {

        System.out.println("==================================");
        System.out.println("Starting Workers...");
        System.out.println("==================================");

        for (Worker worker : workers) {
            executor.submit(worker);
        }
    }

    public void stop() {

        System.out.println("\n==================================");
        System.out.println("Gracefully stopping workers...");
        System.out.println("==================================");

        for (Worker worker : workers) {
            worker.stop();
        }


        executor.shutdown();

        try {

            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {

                System.out.println("Workers did not finish in time.");

                executor.shutdownNow();

                if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                    System.out.println("Forced shutdown complete.");
                }
            }

        } catch (InterruptedException e) {

            executor.shutdownNow();

            Thread.currentThread().interrupt();
        }

        System.out.println("==================================");
        System.out.println("Worker Manager Stopped");
        System.out.println("==================================");
    }
}