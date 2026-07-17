package org.navneet.queuectl.cli;

import org.navneet.queuectl.ConfigRepository;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(
        name = "set",
        description = "Set a configuration value, e.g. 'config set max-retries 3' or 'config set backoff-base 2'"
)
public class ConfigSetCommand implements Runnable {

    @Parameters(index = "0", description = "Config key: max-retries or backoff-base")
    private String key;

    @Parameters(index = "1", description = "Config value (positive integer)")
    private String value;

    private final ConfigRepository configRepository = new ConfigRepository();

    private static final java.util.Set<String> KNOWN_KEYS =
            java.util.Set.of("max-retries", "backoff-base","job-timeout");

    @Override
    public void run() {
        if (!KNOWN_KEYS.contains(key)) {
            System.err.println("Error: \"" + key + "\" is not a recognised config key. Known keys: " + KNOWN_KEYS);
            return;
        }

        // max-retries must be a positive integer; backoff-base must be a positive integer >= 2
        try {
            int intValue = Integer.parseInt(value);
            if (intValue < 1) {
                System.err.println("Error: value must be a positive integer, got \"" + value + "\".");
                return;
            }
            if (key.equals("backoff-base") && intValue < 2) {
                System.err.println("Error: backoff-base must be >= 2 (got \"" + value + "\").");
                return;
            }
            if (key.equals("job-timeout") && intValue < 0) {
                System.err.println("Error: job-timeout must be >= 0.");
                return;
            }
        } catch (NumberFormatException e) {
            System.err.println("Error: value must be a whole number, got \"" + value + "\".");
            return;
        }

        configRepository.set(key, value);
        System.out.println("Config updated: " + key + " = " + value);
    }
}