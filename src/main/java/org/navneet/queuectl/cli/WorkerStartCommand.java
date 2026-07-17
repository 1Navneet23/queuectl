package org.navneet.queuectl.cli;

import org.navneet.queuectl.JobRepository;
import org.navneet.queuectl.WorkerFlag;
import org.navneet.queuectl.WorkerManager;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.concurrent.CountDownLatch;

@Command(
        name = "start",
        description = "Start worker processes"
)
public class WorkerStartCommand implements Runnable {

    @Option(names = "--count", description = "Number of worker threads to start", defaultValue = "1")
    private int count;

    @Override
    public void run() {
        if (count < 1) {
            System.err.println("Error: --count must be at least 1.");
            return;
        }

        // Clear any stale flag left over from a previous crashed run
        WorkerFlag.remove();

        JobRepository jobRepository = new JobRepository();
        WorkerManager manager = new WorkerManager(count, jobRepository);

        CountDownLatch shutdownLatch = new CountDownLatch(1);

        // Handles Ctrl+C in this same terminal
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            manager.stop();
            WorkerFlag.remove();
        }));

        // Watches for `queuectl worker stop` run from another terminal
        Thread flagWatcher = new Thread(() -> {
            try {
                while (!WorkerFlag.exists()) {
                    Thread.sleep(1000);
                }
                System.out.println("\nStop signal received.");
                manager.stop();
                WorkerFlag.remove();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                shutdownLatch.countDown();
            }
        });
        flagWatcher.setDaemon(true);

        manager.start();
        flagWatcher.start();

        System.out.println(count + " worker(s) started. Run 'queuectl worker stop' in another terminal to stop, or press Ctrl+C.");

        try {
            shutdownLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("Worker process exiting.");
    }
}