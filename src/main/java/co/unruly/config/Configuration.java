package co.unruly.config;

import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

public class Configuration {

    private ConfigurationSource func = (key) -> null;

    public Configuration(ConfigurationSource map) {
        this.func = map;
    }

    public static Configuration from(ConfigurationSource func) {
        return new Configuration(func);
    }

    public Optional<String> get(String s) {
        return Optional.ofNullable(func.get(s));
    }

    public Configuration or(ConfigurationSource File) {
        this.func = this.func.or(File);
        return this;
    }

    public static ConfigurationSource map(Map<String, String> map){
        return map::get;
    };

    public static ConfigurationSource properties(String s) {
        Properties properties = new Properties();

        try {
            properties.load(new FileReader(s));
        } catch (IOException e) {
           throw new ConfigurationSetUpException(e);
        }

        return properties::getProperty;
    }
}

@FunctionalInterface
interface ConfigurationSource {

    String get(String key);

    default ConfigurationSource or(ConfigurationSource source) {
        return (key) -> Optional
            .ofNullable(this.get(key))
            .orElse(source.get(key));
    }
}
