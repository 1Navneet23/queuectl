package org.navneet.queuectl.cli;

import org.navneet.queuectl.ConfigRepository;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(
        name = "get",
        description = "Get a configuration value, e.g. 'config get max-retries'"
)
public class ConfigGetCommand implements Runnable {

    @Parameters(index = "0", description = "Config key (e.g. max-retries, backoff-base)")
    private String key;

    private final ConfigRepository configRepository = new ConfigRepository();

    @Override
    public void run() {
        String value = configRepository.get(key, null);
        if (value == null) {
            System.out.println(key + " is not set (no default applied here).");
        } else {
            System.out.println(key + " = " + value);
        }
    }
}