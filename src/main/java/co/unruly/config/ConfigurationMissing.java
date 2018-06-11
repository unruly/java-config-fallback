package co.unruly.config;

public class ConfigurationMissing extends RuntimeException {

    public ConfigurationMissing(String property) {
        super(property + " not found in configuration");
    }
}
