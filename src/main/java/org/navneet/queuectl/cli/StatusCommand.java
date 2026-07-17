package org.navneet.queuectl.cli;

import picocli.CommandLine.Command;

@Command(name = "status", description = "Show summary of all job states & active workers")
public class StatusCommand implements Runnable {
    @Override
    public void run() {
        System.out.println("status placeholder - not implemented yet");
    }
}