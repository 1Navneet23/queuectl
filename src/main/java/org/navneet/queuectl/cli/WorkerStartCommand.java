package org.navneet.queuectl.cli;

import picocli.CommandLine.Command;

@Command(
        name = "start",
        description = "Start worker processes"
)
public class WorkerStartCommand implements Runnable {

    @Override
    public void run() {
        System.out.println("worker start placeholder - not implemented yet");
    }
}