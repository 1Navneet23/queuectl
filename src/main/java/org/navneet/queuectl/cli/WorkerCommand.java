package org.navneet.queuectl.cli;

import picocli.CommandLine.Command;

@Command(
        name = "worker",
        description = "Manage workers",
        subcommands = {
                WorkerStartCommand.class,
                WorkerStopCommand.class
        }
)
public class WorkerCommand implements Runnable {

    @Override
    public void run() {
        System.out.println("Use a worker subcommand: start or stop");
    }
}