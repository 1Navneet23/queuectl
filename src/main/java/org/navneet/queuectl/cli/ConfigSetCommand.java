package org.navneet.queuectl.cli;

import org.navneet.queuectl.ConfigRepository;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(
        name = "set",
        description = "Set a configuration value, e.g. 'config set max-retries 3'"
)
public class ConfigSetCommand implements Runnable {

    @Parameters(index = "0", description = "Config key (e.g. max-retries, backoff-base)")
    private String key;

    @Parameters(index = "1", description = "Config value")
    private String value;

    private final ConfigRepository configRepository = new ConfigRepository();

    private static final java.util.Set<String> KNOWN_KEYS =
            java.util.Set.of("max-retries", "backoff-base");

    @Override
    public void run() {
        if (!KNOWN_KEYS.contains(key)) {
            System.out.println("Warning: \"" + key + "\" is not a recognized config key. Known keys: " + KNOWN_KEYS);
        }

        try {
            Integer.parseInt(value);
        } catch (NumberFormatException e) {
            System.err.println("Error: value must be a whole number, got \"" + value + "\".");
            return;
        }

        configRepository.set(key, value);
        System.out.println("Config updated: " + key + " = " + value);
    }
}