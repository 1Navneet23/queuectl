package org.navneet.queuectl.cli;

import picocli.CommandLine.Command;

@Command(
        name = "list",
        description = "List jobs in the queue"
)
public class ListCommand implements Runnable {

    @Override
    public void run() {
        System.out.println("list placeholder - not implemented yet");
    }
}