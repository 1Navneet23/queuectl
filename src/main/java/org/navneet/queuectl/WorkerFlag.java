package org.navneet.queuectl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class WorkerFlag {

    private static final Path FLAG_PATH = Path.of("worker.flag");

    public static void create() {
        try {
            if (!Files.exists(FLAG_PATH)) {
                Files.createFile(FLAG_PATH);
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to create worker flag.", e);
        }
    }

    public static void remove() {
        try {
            Files.deleteIfExists(FLAG_PATH);
        } catch (IOException e) {
            throw new RuntimeException("Unable to remove worker flag.", e);
        }
    }

    public static boolean exists() {
        return Files.exists(FLAG_PATH);
    }
}

//q enqueue "{\"id\":\"job-success\",\"command\":\"echo Hello QueueCTL\"}"
//q enqueue "{\"id\":\"job-fail\",\"command\":\"invalidcommand\"}"