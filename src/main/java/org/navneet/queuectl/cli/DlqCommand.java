package org.navneet.queuectl.cli;

import org.navneet.queuectl.cli.DlqListCommand;
import org.navneet.queuectl.cli.DlqRetryCommand;
import picocli.CommandLine.Command;

@Command(
        name = "dlq",
        description = "Manage dead letter queue",
        subcommands = {
                DlqListCommand.class,
                DlqRetryCommand.class
        }
)
public class DlqCommand implements Runnable {

    @Override
    public void run() {
        System.out.println("Use a dlq subcommand: list or retry");
    }
}