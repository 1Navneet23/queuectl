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

        this.executor = Executors.newFixedThreadPool(workerCount);
        this.workers = new ArrayList<>();

        for (int i = 1; i <= workerCount; i++) {

            Worker worker =
                    new Worker("worker-" + i, repository);

            workers.add(worker);
        }
    }

    public void start() {

        System.out.println("Starting workers...");

        for (Worker worker : workers) {
            executor.submit(worker);
        }
    }

    public void stop() {

        System.out.println("Stopping workers...");

        for (Worker worker : workers) {
            worker.stop();
        }

        executor.shutdown();

        try {

            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {

                System.out.println("Force shutting down workers...");

                executor.shutdownNow();
            }

        } catch (InterruptedException e) {

            executor.shutdownNow();

            Thread.currentThread().interrupt();
        }

        System.out.println("All workers stopped.");
    }
}