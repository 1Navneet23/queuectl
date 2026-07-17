package org.navneet.queuectl.cli;

import picocli.CommandLine.Command;

@Command(
        name = "config",
        description = "View and update queue configuration",
        subcommands = {
                ConfigSetCommand.class,
                ConfigGetCommand.class
        }
)
public class ConfigCommand implements Runnable {

    @Override
    public void run() {
        System.out.println("Use a config subcommand: set or get");
    }
}