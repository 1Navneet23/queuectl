package org.navneet.queuectl.cli;

import picocli.CommandLine.Command;

@Command(
        name = "list",
        description = "List dead letter queue jobs"
)
public class DlqListCommand implements Runnable {

    @Override
    public void run() {
        System.out.println("dlq list placeholder - not implemented yet");
    }
}