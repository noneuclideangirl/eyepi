package net.noneuclideangirl.util;

import net.noneuclideangirl.functional.Option;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ConfigManager {
    private static final Logger log = LogManager.getLogger(ConfigManager.class);
    private static final OnceAssignable<PropertiesConfiguration> config = new OnceAssignable<>();

    public static void loadProperties(String filename) {
        FileBasedConfigurationBuilder<PropertiesConfiguration> builder =
                new FileBasedConfigurationBuilder<>(PropertiesConfiguration.class)
                .configure(new Parameters().properties().setFileName(filename));
        try {
            config.set(builder.getConfiguration());
        } catch (ConfigurationException e) {
            log.error("Failed loading configuration file \"" + filename + "\": " + e.getMessage());
            System.exit(-1);
        }
    }

    public static Option<String> getString(String key) {
        return Option.of(config.get().getString(key));
    }
    public static Option<Integer> getInt(String key) {
        return Option.of(config.get().getInt(key));
    }
}
