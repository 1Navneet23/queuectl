package org.navneet.queuectl.cli;

import org.navneet.queuectl.WorkerFlag;
import picocli.CommandLine.Command;

@Command(
        name = "stop",
        description = "Stop running workers"
)
public class WorkerStopCommand implements Runnable {

    @Override
    public void run() {
        if (WorkerFlag.exists()) {
            System.out.println("Stop signal already pending.");
            return;
        }
        WorkerFlag.create();
        System.out.println("Stop signal sent. Running workers will finish their current job and exit.");
    }
}