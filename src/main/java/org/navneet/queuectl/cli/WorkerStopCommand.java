package org.navneet.queuectl.cli;

import picocli.CommandLine.Command;

@Command(
        name = "stop",
        description = "Stop running workers"
)
public class WorkerStopCommand implements Runnable {

    @Override
    public void run() {
        System.out.println("worker stop placeholder - not implemented yet");
    }
}