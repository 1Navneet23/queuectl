package org.navneet.queuectl.cli;

import picocli.CommandLine.Command;

@Command(
        name = "config",
        description = "View and update queue configuration"
)
public class ConfigCommand implements Runnable {

    @Override
    public void run() {
        System.out.println("config placeholder - not implemented yet");
    }
}