package org.navneet.queuectl;

import org.navneet.queuectl.cli.*;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
        name = "queuectl",
        mixinStandardHelpOptions = true,
        version = "1.0",
        description = "SQLite-backed job queue",
        subcommands = {
                EnqueueCommand.class,
                WorkerCommand.class,
                StatusCommand.class,
                ListCommand.class,
                DlqCommand.class,
                ConfigCommand.class
        }
)
public class Main implements Runnable {
    public static void main(String[] args) {
        Database.createTable();
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }
    @Override
    public void run() {
        CommandLine.usage(this, System.out);
    }
}