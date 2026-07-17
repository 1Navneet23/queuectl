package org.navneet.queuectl.cli;

import picocli.CommandLine.Command;

@Command(
        name = "retry",
        description = "Retry a dead letter queue job"
)
public class DlqRetryCommand implements Runnable {

    @Override
    public void run() {
        System.out.println("dlq retry placeholder - not implemented yet");
    }
}